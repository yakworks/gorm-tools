/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import java.util.function.Supplier

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.Datastore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.async.AsyncConfig
import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelTools
import gorm.tools.beans.map.MetaMapEntityService
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.model.DataOp
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.commons.map.Maps
import yakworks.problem.ProblemTrait

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@CompileStatic
trait BulkableRepo<D> {

    final private static Logger log = LoggerFactory.getLogger(BulkableRepo)

    @Autowired(required = false)
    SyncJobService syncJobService

    @Autowired
    @Qualifier("parallelTools")
    ParallelTools parallelTools

    @Autowired
    AsyncService asyncService

    @Autowired
    MetaMapEntityService metaMapEntityService

    @Autowired
    ProblemHandler problemHandler

    //Here for @CompileStatic - GormRepo implements these
    abstract D create(Map data, Map args)
    abstract D update(Map data, Map args)
    abstract D doCreate(Map data, Map args)
    abstract D doUpdate(Map data, Map args)
    abstract  Class<D> getEntityClass()
    abstract void flushAndClear()
    abstract void clear()
    abstract Datastore getDatastore()
    abstract <T> T withTrx(Closure<T> callable)

    /**
     * Allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * @param dataList the list of data maps to create
     * @param bulkablArgs the args object to pass on to doBulk
     * @return Job
     */
    Long bulk(List<Map> dataList, BulkableArgs bulkablArgs = new BulkableArgs()) {
        Map params = bulkablArgs.params
        Long jobId = syncJobService.createJob((String)params.source, (String)params.sourceId, dataList)

        def supplierFunc = { doBulkParallel(dataList, bulkablArgs) } as Supplier<ApiResults>
        def asyncArgs = new AsyncConfig(enabled: bulkablArgs.asyncEnabled)

        asyncService.supplyAsync(asyncArgs, supplierFunc)
            .whenComplete { ApiResults results, ex ->
                if(ex){ //should never really happen as we should have already handled them
                    results << problemHandler.handleUnexpected(ex)
                }
                finishJob(jobId, results, bulkablArgs.includes)
            }

        return jobId
    }

    ApiResults doBulkParallel(List<Map> dataList, BulkableArgs bulkablArgs){
        ApiResults results = ApiResults.create()
        List<Collection<Map>> sliceErrors = Collections.synchronizedList([] as List<Collection<Map>> )

        AsyncConfig pconfig = AsyncConfig.of(getDatastore())
        pconfig.enabled = bulkablArgs.asyncEnabled //same as above, ability to override through params

        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(pconfig, dataList) { dataSlice ->
            try {
                withTrx {
                    ApiResults res = doBulk((List<Map>) dataSlice, bulkablArgs)
                    results.merge res
                }
            } catch(Exception e) {
                //on pass1 we collect the slices that failed and will run through them again with each item in its own trx
                sliceErrors.add(dataSlice)
            }
        }
        // if it has slice errors try again but this time run each item in its own transaction
        if(sliceErrors.size()) {
            AsyncConfig asynArgsNoTrx = AsyncConfig.of(getDatastore())
            parallelTools.each(asynArgsNoTrx, sliceErrors) { dataSlice ->
                try {
                    results.merge doBulk((List<Map>) dataSlice, bulkablArgs, true)
                } catch(Exception ex) {
                    // just in case, unexpected errors as we should have intercepted them all already in doBulk
                    results << problemHandler.handleUnexpected(ex)
                }
            }
        }
        return results
    }


    /**
     * Does the bulk create/update, normally will be passing in a slice of data.
     * this should be wrapped in a transaction
     * Flushes and clears at the end so errors show up in the right place
     *
     * @param dataList the data chunk
     * @param bulkablArgs the persist args to pass to repo methods
     * @param transactionalItem default=false which assumes this method is wrapped in a trx and
     *        any error processing the dataList will throw error so it rolls back.
     *        if transactionalItem=true then its assumed that this method is not wrapped in a trx,
     *        and transactionalItem value will be passed to createOrUpdate where each item update or create is in its own trx.
     *        also, if true then this method will try not to throw an exception and
     *        it will collect the errors in the results.
     * @return the BulkableResults object with what succeeded and what failed
     */
    ApiResults doBulk(List<Map> dataList, BulkableArgs bulkablArgs, boolean transactionalItem = false){
        ApiResults results = ApiResults.create(false)
        for (Map item : dataList) {
            Map itemCopy
            D entityInstance

            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                //or it can create circular references - eg org.contact.org - which would result in Stackoverflow when converting to json
                    itemCopy = Maps.deepCopy(item)
                boolean isCreate = bulkablArgs.op == DataOp.add
                entityInstance = createOrUpdate(isCreate, transactionalItem, itemCopy, bulkablArgs.persistArgs)
                results << Result.of(entityInstance).status(isCreate ? 201 : 200)
            } catch(Exception e) {
                // if trx by item then collect the exceptions, otherwise throw so it can rollback
                if(transactionalItem){
                    results << problemHandler.handleException(e).payload(item)
                } else {
                    throw e
                }
            }
        }
        // flush and clear here so easier to debug problems and clear for memory to help garbage collection
        if(getDatastore().hasCurrentSession()) {
            // if trx is at item then only clear
            transactionalItem ? clear() : flushAndClear()
        }

        return results
    }


    D createOrUpdate(boolean isCreate, boolean transactional, Map data, Map persistArgs){
        D entityInstance
        if(transactional){
            entityInstance = isCreate ? create(data, persistArgs) : update(data, persistArgs)
        } else{
            entityInstance = isCreate ? doCreate(data, persistArgs) : doUpdate(data, persistArgs)
        }
        return entityInstance
    }

    void finishJob(Long jobId, ApiResults results, List includes){
        List<Map> jsonResults = transformResults(results, includes?:['id'])
        syncJobService.updateJob(jobId, SyncJobState.Finished, results, jsonResults)
    }

    /**
     * transform results to list of maps, see above.
     * @param customizer closure that ruturns a map that should be merged in, runs for each item in results
     */
    List<Map> transformResults(ApiResults results, List includes) {
        List<Map> ret = []
        boolean ok = true
        for (Result r : results) {
            def map = [ok: r.ok, status: r.status.code] as Map<String, Object>
            //do the failed
            if (r instanceof ProblemTrait) {
                map.putAll([
                    data: r.payload,
                    title: r.title,
                    detail: r.detail,
                    errors: r.violations
                ])
            } else {
                def entityObj = r.payload
                Map entityMapData = metaMapEntityService.createMetaMap(entityObj, includes) as Map<String, Object>
                map.data = entityMapData
            }
            ret << map
        }
        return ret
    }

}
