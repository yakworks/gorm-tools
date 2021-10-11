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

import gorm.tools.async.Futures
import gorm.tools.async.ParallelConfig
import gorm.tools.async.ParallelTools
import gorm.tools.beans.EntityMapService
import gorm.tools.job.JobRepoTrait
import gorm.tools.job.JobState
import gorm.tools.job.JobTrait
import gorm.tools.repository.errors.api.ApiErrorHandler
import gorm.tools.repository.model.DataOp
import gorm.tools.transaction.TrxService
import yakworks.commons.map.Maps

import static gorm.tools.repository.bulk.BulkableResults.Result

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
    EntityMapService entityMapService

    @Autowired
    TrxService trxService

    @Autowired
    ApiErrorHandler apiErrorHandler

    //Here for @CompileStatic - GormRepo implements these
    abstract D doCreate(Map data, Map args)
    abstract D doUpdate(Map data, Map args)
    abstract  Class<D> getEntityClass()
    abstract void flushAndClear()
    abstract Datastore getDatastore()

    /**
     * Allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * @param dataList the list of data maps to create
     * @param bulkablArgs the args object to pass on to doBulk
     * @return Job
     */
    J bulk(List<Map> dataList, BulkableArgs bulkablArgs = new BulkableArgs()) {

        J job = (J) jobRepo.create(bulkablArgs.jobSource, bulkablArgs.jobSourceId, dataList)
        //keep the jobId around
        Long jobId = job.id

        def supplierFunc = { doBulkParallel(dataList, bulkablArgs) } as Supplier<BulkableResults>

        Futures.of(bulkablArgs.async, supplierFunc).whenComplete{ BulkableResults results, ex ->
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

        def asynArgs = new ParallelConfig(transactional:true, datastore: getDatastore())
        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(asynArgs, dataList) { dataSlice ->
            try {
                results.merge doBulkSlice((List<Map>) dataSlice, bulkablArgs)
            } catch(Exception e) {
                //catch any errors which may occur during flush/commit
                def apiError = apiErrorHandler.handleException(getEntityClass(), e)
                Result.of(apiError, dataSlice).addTo(results)
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
     * @param args the persist args to pass to repo methods
     * @return the BulkableResults object with what succeeded and what failed
     */
    BulkableResults doBulkSlice(List<Map> dataList, BulkableArgs bulkablArgs){
        def results = new BulkableResults(false)
        for (Map item : dataList) {
            Map itmCopy
            D entityInstance
            BulkableResults r
            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                itmCopy = Maps.deepCopy(item)
                entityInstance = bulkablArgs.op == DataOp.add ? doCreate(item, bulkablArgs.persistArgs) : doUpdate(item, bulkablArgs.persistArgs)
                Result.of(entityInstance, 201).addTo(results)
            } catch(Exception e) {
                // XXX I think we should be throwing here so it can roll back, then per Joanna we can recycle through
                // each item in its own transaction
                def apiError = apiErrorHandler.handleException(getEntityClass(), e)
                Result.of(apiError, itmCopy).addTo(results)
            }
        }
        //end of transaction, flush here so easier to debug problems and clear for memory to help garbage collection
        flushAndClear()
        return results
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
