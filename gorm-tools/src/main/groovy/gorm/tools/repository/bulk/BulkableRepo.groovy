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
import gorm.tools.beans.EntityMapService
import gorm.tools.job.JobRepoTrait
import gorm.tools.job.JobState
import gorm.tools.job.JobTrait
import gorm.tools.repository.errors.api.ApiError
import gorm.tools.repository.errors.api.ApiErrorHandler
import gorm.tools.repository.model.DataOp
import gorm.tools.transaction.TrxService
import yakworks.commons.map.Maps

import static gorm.tools.repository.bulk.BulkableResults.Result
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@CompileStatic
trait BulkableRepo<D, J extends JobTrait>  {

    final private static Logger log = LoggerFactory.getLogger(BulkableRepo)

    @Autowired(required = false)
    JobRepoTrait jobRepo

    @Autowired
    @Qualifier("parallelTools")
    ParallelTools parallelTools

    @Autowired
    AsyncService asyncService

    @Autowired
    EntityMapService entityMapService

    @Autowired
    TrxService trxService

    @Autowired
    ApiErrorHandler apiErrorHandler

    //Here for @CompileStatic - GormRepo implements these
    abstract D create(Map data, Map args)
    abstract D update(Map data, Map args)
    abstract D doCreate(Map data, Map args)
    abstract D doUpdate(Map data, Map args)
    abstract  Class<D> getEntityClass()
    abstract void flushAndClear()
    abstract void clear()
    abstract Datastore getDatastore()

    /**
     * Allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * @param dataList the list of data maps to create
     * @param bulkablArgs the args object to pass on to doBulk
     * @return Job
     */
    J bulk(List<Map> dataList, BulkableArgs bulkablArgs = new BulkableArgs()) {
        Map params = bulkablArgs.params
        J job = (J) jobRepo.create((String)params.source, (String)params.sourceId, dataList)
        //keep the jobId around
        Long jobId = job.id

        def supplierFunc = { doBulkParallel(dataList, bulkablArgs) } as Supplier<BulkableResults>
        def asyncArgs = new AsyncConfig(enabled: bulkablArgs.asyncEnabled)

        asyncService.supplyAsync(asyncArgs, supplierFunc)
            .whenComplete { BulkableResults results, ex ->
                if(ex){ //should never really happen
                    def apiError = apiErrorHandler.handleException(getEntityClass(), ex)
                    Result.of(apiError, null).addTo(results)
                }
                finishJob(jobId, results, bulkablArgs.includes)
            }

        // return job
        return (J) jobRepo.get(jobId)

    }

    BulkableResults doBulkParallel(List<Map> dataList, BulkableArgs bulkablArgs){
        def results = new BulkableResults()
        List<Collection<Map>> sliceErrors = Collections.synchronizedList([] as List<Collection<Map>> )

        def pconfig = new AsyncConfig(transactional:true, datastore: getDatastore())
        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(pconfig, dataList) { dataSlice ->
            try {
                def res = doBulk((List<Map>) dataSlice, bulkablArgs)
                results.merge(res)
            } catch(Exception e) {
                //on pass1 we collect the slices that failed and will run through them again with each item in its own trx
                //XXX Johsh - here is the problem - we catch the exception and proceed, so exception isnt crossing transaction boundry
                //if we put breakpoint in GrailsTransactionTemplate.execute, we can see that transaction gets committed instead of rollback.
                sliceErrors.add(dataSlice)
            }
        }
        // if it has slice errors try again but this time run each item in its own transaction
        if(sliceErrors.size()) {
            def asynArgsNoTrx = AsyncConfig.of(getDatastore())
            parallelTools.each(asynArgsNoTrx, sliceErrors) { dataSlice ->
                try {
                    results.merge doBulk((List<Map>) dataSlice, bulkablArgs, true)
                } catch(Exception e) {
                    // this is an unexpected errors and should not happen, we should have intercepted them all already
                    log.error(e.message, e)
                    def apiError = new ApiError(INTERNAL_SERVER_ERROR, "Internal Server Error", e.message)
                    Result.of(apiError, dataSlice).addTo(results)
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
     * @param transactionalItem defaults to false which assumes this method is wrapped in a trx and
     *        any error processing the dataList will throw error so it rollsback.
     *        if true then its assumed that this method is not wrapped in a trx, and transactionalItem value
     *        will be passed to createOrUpdate where each item update or create is in its own trx.
     *        also, if true then this method will try not to throw an exception and
     *        it will collect the errors in the results.
     * @return the BulkableResults object with what succeeded and what failed
     */
    BulkableResults doBulk(List<Map> dataList, BulkableArgs bulkablArgs, boolean transactionalItem = false){
        def results = new BulkableResults(false)
        for (Map item : dataList) {
            Map itemCopy
            D entityInstance
            BulkableResults r
            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                //or it can create circular references - eg org.contact.org - which would result in Stackoverflow when converting to json
                itemCopy = Maps.deepCopy(item)
                boolean isCreate = bulkablArgs.op == DataOp.add
                entityInstance = createOrUpdate(isCreate, transactionalItem, itemCopy, bulkablArgs.persistArgs)
                Result.of(entityInstance, isCreate ? 201 : 200).addTo(results)
            } catch(Exception e) {
                // if trx by item then collect the execeptions, otherwise throw so it can rollback
                if(transactionalItem){
                    def apiError = apiErrorHandler.handleException(getEntityClass(), e)
                    Result.of(apiError, item).addTo(results)
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

    J finishJob(Long jobId, BulkableResults results, List includes){
        List<Map> jsonResults = transformResults(results, includes?:['id'])
        jobRepo.update(jobId, JobState.Finished, results, jsonResults)
    }

    /**
     * transforms the BulkableResults to a list of maps that can be serialzied or rendered
     *
     * @param results the BulkableResults
     * @param includes if results are successfull then this is the entity inlcludes on what fields to serialize
     * @return the transformed list of data maps
     */
    List<Map> transformResults(BulkableResults results, List includes){
        // the transform closure will run for each enty in results list
        List<Map> jsonResults = results.transform(includes){  result ->
            result = result as BulkableResults.Result //compiler is getting confused on type
            //successful result would have entity, use the includes list to prepare result object
            if(result.ok) {
                def data = entityMapService.createEntityMap(result.entityObject, includes) as Map<String, Object>
                return [data: data]
            }
            return [:]
        }
    }

}
