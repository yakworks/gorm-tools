/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.async.AsyncSupport
import gorm.tools.beans.EntityMapService
import gorm.tools.job.JobRepoTrait
import gorm.tools.job.JobState
import gorm.tools.job.JobTrait
import gorm.tools.json.Jsonify
import gorm.tools.repository.errors.api.ApiErrorHandler
import gorm.tools.transaction.TrxService
import yakworks.commons.map.Maps

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@CompileStatic
trait BulkableRepo<D, J extends JobTrait>  {

    @Autowired(required = false)
    JobRepoTrait jobRepo

    @Autowired
    @Qualifier("asyncSupport")
    AsyncSupport asyncSupport

    @Autowired
    EntityMapService entityMapService

    @Autowired
    TrxService trxService

    //Here for @CompileStatic - GormRepo implements it
    abstract D doCreate(Map data, Map args)
    abstract  Class<D> getEntityClass()


    /**
     * Allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * @param dataList the list of data maps to create
     * @param args args to pass to doCreate. It can have:
     *      source -  what to set the job.source to
     *      includes - for result, list of fields to include for the created or updated entity
     *      errorThreshold - (default: false) number of errors before it stops the job. this setting ignored if transactional=true
     *      transactional - (default: false) if true then the whole set should be in a transaction. disables parallelProcessing.
     *          will disable parallelProcessing
     *      async - (default: true) whether it should return thr job imediately or do it sync
     * @return Job
     */
    J bulkCreate(List<Map> dataList, Map args = [:]) {

        //FIXME #339 make a conrete create method that we can pass this stuff too
        // rename
        J job = (J) jobRepo.create([source: args.source, state: JobState.Running, dataPayload: dataList], [flush:true])

        //keep the jobId around
        Long jobId = job.id

        def results = new BulkableResult.Results()

        Closure bulkCreateClosure = { List<Map> dataChunk ->
            results.merge doBulkCreateTrx(dataChunk, args)
        }

        asyncSupport.parallelChunks(dataList, bulkCreateClosure)

        updateJobResults(jobId, results, args.includes as List)

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


    BulkableResult.Results doBulkCreateTrx(List<Map> dataList, Map args = [:]){
        trxService.withTrx {
            doBulkCreate(dataList, args)
        }
    }

    BulkableResult.Results doBulkCreate(List<Map> dataList, Map args = [:]){
        def results = new BulkableResult.Results()
        for (Map item : dataList) {
            Map itmCopy
            D entityInstance
            BulkableResult r
            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                itmCopy = Maps.deepCopy(item)
                entityInstance = doCreate(item, args)
                BulkableResult.of(entityInstance, 201).addTo(results)
            } catch(Exception e) {
                def apiError = ApiErrorHandler.handleException(getEntityClass(), e)
                BulkableResult.of(apiError, itmCopy).addTo(results)
            }
        }
        return results
    }

    void updateJobResults(Long jobId, BulkableResult.Results results, List includes){
        List<Map> jsonResults = results.transform {
            //successful result would have entity, use the includes list to prepare result object
            def data = entityMapService.createEntityMap(it.entityObject, includes?:['id']) as Map<String, Object>
            return [id: data['id'], data: data]
        }
        byte[] resultBytes = Jsonify.render(jsonResults).jsonText.bytes

        jobRepo.update([id:jobId, ok: results.ok, results: resultBytes, state: JobState.Finished], [flush: true])
    }

}
