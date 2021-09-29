/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.Datastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.async.ParallelConfig
import gorm.tools.async.ParallelTools
import gorm.tools.beans.EntityMapService
import gorm.tools.job.JobRepoTrait
import gorm.tools.job.JobState
import gorm.tools.job.JobTrait
import gorm.tools.repository.errors.api.ApiError
import gorm.tools.repository.errors.api.ApiErrorHandler
import gorm.tools.transaction.TrxService
import yakworks.commons.map.Maps

import static gorm.tools.repository.bulk.BulkableResults.Result

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@CompileStatic
trait BulkableRepo<D, J extends JobTrait>  {

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
    abstract  Class<D> getEntityClass()
    abstract void flushAndClear()
    abstract Datastore getDatastore()

    /**
     * Allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * @param dataList the list of data maps to create
     * @param args args to pass to doCreate. It can have:
     *      jobSource -  what to set the job.source to
     *      includes - for successful result, list of fields to include for the created or updated entity
     *      errorThreshold - (default: false) number of errors before it stops the job. this setting ignored if transactional=true
     *      transactional - (default: false) if true then the whole set should be in a transaction. disables parallelProcessing.
     *          will disable parallelProcessing
     *      async - (default: true) whether it should return thr job imediately or do it sync
     * @return Job
     */
    J bulkCreate(List<Map> dataList, BulkableArgs bulkablArgs = new BulkableArgs()) {

        J job = (J) jobRepo.create(bulkablArgs.jobSource, bulkablArgs.jobSourceId, dataList)

        //keep the jobId around
        Long jobId = job.id

        def results = new BulkableResults()

        def asynArgs = new ParallelConfig(transactional:true, datastore: getDatastore())
        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(asynArgs, dataList) { dataChunk ->
            try {
                results.merge doBulkCreate((List<Map>) dataChunk, bulkablArgs.persistArgs)
            } catch(Exception e) {
                results.addGlobalError apiErrorHandler.handleException(getEntityClass(), e)
            }
        }

        finishJob(jobId, results, bulkablArgs.includes)

        // return job
        return (J) jobRepo.get(jobId)

    }

    //FIXME #339 implement
    // J bulkCreatePromise(List<Map> dataList, Map args = [:]) {
    //
    //     Promise promise = task {
    //
    //         //doBulkCreate(bulkResults, dataList, args)
    //
    //     }.onComplete { result ->
    //         updateJobResults(jobId, bulkResults, args.includes as List)
    //     }.onError { Throwable err ->
    //         //!! should nver get here? log to Job?
    //     }
    //
    // }


    /**
     * Does the bulk create, normally will be passing in a slice of data and this will be wrapped in a transaction
     * Flushes and clears at the end so errors show up in the right place
     *
     * @param dataList the data chunk
     * @param args the persist args to pass to repo methods
     * @return the BulkableResults object with what succeeded and what failed
     */
    BulkableResults doBulkCreate(List<Map> dataList, Map args = [:]){
        def results = new BulkableResults(false)
        for (Map item : dataList) {
            Map itmCopy
            D entityInstance
            BulkableResults r
            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                itmCopy = Maps.deepCopy(item)
                entityInstance = doCreate(item, args)
                Result.of(entityInstance, 201).addTo(results)
            } catch(Exception e) {
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
