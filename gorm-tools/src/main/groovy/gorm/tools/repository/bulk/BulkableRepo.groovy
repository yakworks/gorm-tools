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
import gorm.tools.beans.map.MetaMap
import gorm.tools.beans.map.MetaMapEntityService
import gorm.tools.databinding.PathKeyMap
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.model.DataOp
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.commons.map.Maps

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
    abstract <T> T withNewTrx(Closure<T> callable)

    /**
     * Allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * @param dataList the list of data maps to create
     * @param syncJobArgs the args object to pass on to doBulk
     * @return Job
     */
    Long bulk(List<Map> dataList, SyncJobArgs syncJobArgs) {
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, dataList)

        def supplierFunc = { doBulkParallel(dataList, jobContext) } as Supplier<ApiResults>
        def asyncArgs = new AsyncConfig(enabled: syncJobArgs.promiseEnabled, session: true)

        asyncService.supplyAsync(asyncArgs, supplierFunc)
            .whenComplete { ApiResults results, ex ->
                if(ex){ //should never really happen as we should have already handled them
                    log.error("BulkableRepo unexpected exception", ex)
                    jobContext.results << problemHandler.handleUnexpected(ex)
                }
                jobContext.finishJob()
            }

        return jobContext.jobId
    }

    void doBulkParallel(List<Map> dataList, SyncJobContext jobContext){
        List<Collection<Map>> sliceErrors = Collections.synchronizedList([] as List<Collection<Map>> )

        AsyncConfig pconfig = AsyncConfig.of(getDatastore())
        pconfig.enabled = jobContext.args.asyncEnabled //same as above, ability to override through params
        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(pconfig, dataList) { dataSlice ->
            try {
                Long chunkStart = System.currentTimeMillis()
                ApiResults results
                withTrx {
                    results = doBulk((List<Map>) dataSlice, jobContext)
                }
                updateJobResults(jobContext, results)

                logTime(chunkStart)

            } catch(Exception e) {
                //on pass1 we collect the slices that failed and will run through them again with each item in its own trx
                sliceErrors.add(dataSlice)
            }
        }

        // if it has slice errors try again but this time run each item in the slice in its own transaction
        if(sliceErrors.size()) {
            AsyncConfig asynArgsNoTrx = AsyncConfig.of(getDatastore())
            asynArgsNoTrx.enabled = jobContext.args.asyncEnabled
            parallelTools.each(asynArgsNoTrx, sliceErrors) { dataSlice ->
                try {
                    ApiResults results = doBulk((List<Map>) dataSlice, jobContext, true)
                    updateJobResults(jobContext, results)
                } catch(Exception ex) {
                    log.error("BulkableRepo unexpected exception", ex)
                    // just in case, unexpected errors as we should have intercepted them all already in doBulk
                    jobContext.results << problemHandler.handleUnexpected(ex)
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
    ApiResults doBulk(List<Map> dataList, SyncJobContext jobContext, boolean transactionPerItem = false){
        // println "will do ${dataList.size()}"
        ApiResults results = ApiResults.create(false)
        for (Map item : dataList) {
            Map itemData
            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                //or it can create circular references - eg org.contact.org - which would result in Stackoverflow when converting to json
                if(item instanceof PathKeyMap){
                    itemData = item.init() //initialize it, this will be from CSV
                } else {
                    itemData = Maps.deepCopy(item)
                }
                boolean isCreate = jobContext.args.op == DataOp.add
                //make sure args has its own copy as GormRepo add data to it and makes changes
                Map args = jobContext.args.persistArgs
                Map entityMapData = createOrUpdate(jobContext, isCreate, transactionPerItem, itemData, args)
                results << Result.of(entityMapData).status(isCreate ? 201 : 200)
            } catch(Exception e) {
                // if trx by item then collect the exceptions, otherwise throw so it can rollback
                if(transactionPerItem){
                    results << problemHandler.handleException(e).payload(item)
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


    Map createOrUpdate(SyncJobContext jobContext, boolean isCreate, boolean transactional, Map data, Map persistArgs) {
        def closure = {
            D entityInstance = isCreate ? doCreate(data, persistArgs) : doUpdate(data, persistArgs)
            return createMetaMap(entityInstance, jobContext)
        } as Closure<Map>

        return transactional ? withTrx(closure) : closure()
    }

    /**
     * uses metaMapEntityService to create the map for the includes in the jobContext.args
     * Will return a clone to ensure that all properties are called and its a clean, unwrapped map
     */
    Map createMetaMap(D entityInstance, SyncJobContext jobContext){
        MetaMap entityMapData = metaMapEntityService.createMetaMap(entityInstance, jobContext.args.includes)
        return (Map)entityMapData.clone()
    }

    void updateJobResults(SyncJobContext jobContext, ApiResults results){
        jobContext.updateJobResults(results)
    }

    void logTime(Long start){
        if(log.isDebugEnabled()){
            Long endTime = System.currentTimeMillis()
            print("doBulk done in ${((endTime - start) / 1000)} - ")
            printUsedMem()
        }
    }

    static void printUsedMem(){
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        //Print used memory
        println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb)
    }
}
