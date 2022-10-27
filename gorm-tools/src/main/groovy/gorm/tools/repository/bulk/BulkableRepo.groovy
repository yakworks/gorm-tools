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

import gorm.tools.async.AsyncArgs
import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelTools
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobService
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.DataOp
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.commons.map.Maps
import yakworks.commons.map.PathKeyMap
import yakworks.meta.MetaMap

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@SuppressWarnings(["Println"])
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
    MetaMapService metaMapService

    @Autowired
    ProblemHandler problemHandler

    //Here for @CompileStatic - GormRepo implements these
    abstract D create(Map data, Map args)
    abstract D update(Map data, Map args)
    abstract D doCreate(Map data, PersistArgs args)
    abstract D doUpdate(Map data, PersistArgs args)
    abstract  Class<D> getEntityClass()
    abstract void flushAndClear()
    abstract void clear()
    abstract Datastore getDatastore()
    abstract <T> T withTrx(Closure<T> callable)
    abstract <T> T withNewTrx(Closure<T> callable)

    /**
     * creates a supplier to wrap doBulkParallel and calls bulk
     * if syncJobArgs.promiseEnabled = true will return right away
     *
     * @param dataList the list of data maps to create
     * @param syncJobArgs the args object to pass on to doBulk
     * @return Job id
     */
    Long bulk(List<Map> dataList, SyncJobArgs syncJobArgs) {
        syncJobArgs.entityClass = getEntityClass()
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, dataList)
        Supplier supplierFunc = () -> doBulkParallel(dataList, jobContext)
        return bulk(supplierFunc, jobContext)
    }

    /**
     * Allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * if jobContext.args.promiseEnabled = true will return right away
     *
     * @param supplierFunc the supplier(Promise) that gets passed to asyncService
     * @param jobContext the jobContext with jobId created
     * @return Job id
     */
    Long bulk(Supplier supplierFunc, SyncJobContext jobContext ) {
        def asyncArgs = new AsyncArgs(enabled: jobContext.args.promiseEnabled, session: true)
        // This is the promise call. Will return immediately is syncJobArgs.promiseEnabled=true
        asyncService
            .supplyAsync(asyncArgs, supplierFunc)
            .whenComplete { res, ex ->
                if(ex){ //should never really happen as we should have already handled any errors
                    log.error("BulkableRepo unexpected exception", ex)
                    jobContext.results << problemHandler.handleUnexpected(ex)
                }
                jobContext.finishJob()
            }

        return jobContext.jobId
    }

    void doBulkParallel(List<Map> dataList, SyncJobContext jobContext){
        List<Collection<Map>> sliceErrors = Collections.synchronizedList([] as List<Collection<Map>> )

        AsyncArgs pconfig = AsyncArgs.of(getDatastore())
        pconfig.enabled = jobContext.args.asyncEnabled //same as above, ability to override through params
        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(pconfig, dataList) { dataSlice ->
            Long startTime = System.currentTimeMillis()
            ApiResults results
            try {
                withTrx {
                    results = doBulk((List<Map>) dataSlice, jobContext.args)
                }
                if(results?.ok) updateJobResults(jobContext, results, startTime)
                // ((List<Map>) dataSlice)*.clear() //clear out so mem can be garbage collected
            } catch(Exception e) {
                //on pass1 we collect the slices that failed and will run through them again with each item in its own trx
                sliceErrors.add(dataSlice)
            }

        }

        // if it has slice errors then try again but
        // this time run each item in the slice in its own transaction
        if(sliceErrors.size()) {
            AsyncArgs asynArgsNoTrx = AsyncArgs.of(getDatastore())
            asynArgsNoTrx.enabled = jobContext.args.asyncEnabled
            parallelTools.each(asynArgsNoTrx, sliceErrors) { dataSlice ->
                try {
                    Long startTime = System.currentTimeMillis()
                    ApiResults results = doBulk((List<Map>) dataSlice, jobContext.args, true)
                    updateJobResults(jobContext, results, startTime)
                    // ((List<Map>) dataSlice)*.clear() //clear out so mem can be garbage collected
                } catch(Exception ex) {
                    log.error("BulkableRepo unexpected exception", ex)
                    // just in case, unexpected errors as we should have intercepted them all already in doBulk
                    jobContext.results << problemHandler.handleException(ex)
                }
            }
        }
    }

    /**
     * Does the bulk create/update, normally will be passing in a slice of data.
     * this should be wrapped in a transaction
     * Flushes and clears at the end so errors show up in the right place
     *
     * @param dataList the data chunk
     * @param syncJobArgs the persist args to pass to repo methods
     * @param transactionPerItem default=false which assumes this method is wrapped in a trx and
     *        any error processing the dataList will throw error so it rolls back.
     *        if transactionalItem=true then its assumed that this method is not wrapped in a trx,
     *        and transactionalItem value will be passed to createOrUpdate where each item update or create is in its own trx.
     *        also, if true then this method will try not to throw an exception and
     *        it will collect the errors in the results.
     */
    ApiResults doBulk(List<Map> dataList, SyncJobArgs syncJobArgs, boolean transactionPerItem = false){
        // println "will do ${dataList.size()}"
        ApiResults results = ApiResults.create(false)
        for (Map item : dataList) {
            Map itemData
            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                //or it can create circular references - eg org.contact.org - which would result in Stackoverflow when converting to json
                if(item instanceof PathKeyMap){
                    // Initialize the PathKey map so that the deep nested structure is created  and repos can expect a deep nested map
                    // Clone after it is initialized, so that it will clone the deep nested structure and not flat map
                    item.init()
                    itemData = item.cloneMap()   //clone it, probably from CSV
                } else {
                    itemData = Maps.clone(item)
                }
                Map entityMapData = createOrUpdate(itemData, syncJobArgs, transactionPerItem)
                results << Result.OK().payload(entityMapData).status(syncJobArgs.isCreate() ? 201 : 200)
            } catch(Exception e) {
                // if trx by item then collect the exceptions, otherwise throw so it can rollback
                if(transactionPerItem){
                    results << problemHandler.handleException(e).payload(buildErrorMap(item, syncJobArgs.errorIncludes))
                } else {
                    clear() //clear cache on error since wont hit below
                    throw e
                }
            }
        }
        // flush and clear here so easier to debug problems and clear for memory to help garbage collection
        if(getDatastore().hasCurrentSession()) {
            // if trx is at item then only clear
            transactionPerItem ? clear() : flushAndClear()
        }

        return results
    }

    /**
     * create or update based on syncJobArgs.op
     *
     * @param data the data
     * @param syncJobArgs - persistArgs and isCreate will be pulled from here
     * @param transactional true if should be wraped in withTrx
     * @return the data map after bing run through createMetaMap using the includes in the syncJobArgs
     */
    Map createOrUpdate(Map data, SyncJobArgs syncJobArgs, boolean transactional) {
        def closure = {
            D entityInstance = syncJobArgs.isCreate() ? doCreate(data, syncJobArgs.persistArgs) : doUpdate(data, syncJobArgs.persistArgs)
            return buildSuccessMap(entityInstance, syncJobArgs.includes)
        } as Closure<Map>

        return transactional ? withTrx(closure) : closure()
    }

    /** creates response map based on bulk include list */
    Map buildSuccessMap(D entityInstance, List<String> includes) {
        return createMetaMap(entityInstance, includes)
    }

    /**
     * The fields to return when bulk fails for the entity, by default, return entire incoming map back.
     */
    Map buildErrorMap(Map originalData, List<String> errorIncludes) {
        if(errorIncludes) {
            return originalData.subMap(errorIncludes)
        } else {
            return originalData
        }
    }

    /**
     * uses metaMapService to create the map for the includes in the jobContext.args
     * Will return a clone to ensure that all properties are called and its a clean, unwrapped map
     */
    Map createMetaMap(D entityInstance, List<String> includes){
        MetaMap entityMapData = metaMapService.createMetaMap(entityInstance, includes)
        return (Map)entityMapData.clone()
    }

    /**
     * calls jobContext.updateJobResults and swallows any unexpected exceptions
     *
     * @param jobContext the current jobContext
     * @param results the results of the current slice
     * @param startTimeMillis the start time in milliseconds used for logging elapsed time
     */
    void updateJobResults(SyncJobContext jobContext, ApiResults results, Long startTimeMillis = null){
        try {
            jobContext.updateJobResults(results, startTimeMillis)
        } catch (e){
            //ok to swallow thi excep since we dont want to disrupt the flow
            log.error("Unexpected error during updateJobResults", e)
        }
    }

}

