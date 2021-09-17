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
     *      jobSource -  what to set the job.source to
     *      includes - for result, list of fields to include for the created or updated entity
     *      errorThreshold - (default: false) number of errors before it stops the job. this setting ignored if transactional=true
     *      transactional - (default: false) if true then the whole set should be in a transaction. disables parallelProcessing.
     *          will disable parallelProcessing
     *      async - (default: true) whether it should return thr job imediately or do it sync
     * @return Job
     */
    J bulkCreate(List<Map> dataList, Map args = [:]) {

        //FIXME #339
        J job = (J) jobRepo.create([source: args.source, state: JobState.Running, dataPayload: dataList], [flush:true])

        //keep the jobId around
        Long jobId = job.id

        List<BulkableResult> bulkResults = Collections.synchronizedList([]) as List<BulkableResult>

        Closure bulkCreateClosure = { List<Map> dataChunk ->
            bulkResults.addAll( doBulkCreateTrx(dataChunk, args))
        }

        asyncSupport.parallelChunks(dataList, bulkCreateClosure)

        updateJobResults(jobId, bulkResults, args.includes as List)

        // return job

        return (J) jobRepo.get(jobId)

    }

    //FIXME implement
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


    List<BulkableResult> doBulkCreateTrx(List<Map> dataList, Map args = [:]){
        trxService.withTrx {
            doBulkCreate(dataList, args)
        }
    }

    List<BulkableResult> doBulkCreate(List<Map> dataList, Map args = [:]){
        List<BulkableResult> resultList = [] as List<BulkableResult>
        for (Map item : dataList) {
            Map itmCopy
            D entityInstance
            BulkableResult r
            try {
                //need to copy the incoming map, as during create(), repos may remove entries from the data map
                itmCopy = Maps.deepCopy(item)
                entityInstance = doCreate(item, args)
                r = BulkableResult.of(entityInstance)
            } catch(Exception e) {
                def apiError = ApiErrorHandler.handleException(getEntityClass(), e)
                r = BulkableResult.of(apiError, itmCopy)
            }
            resultList.add r
        }
        return resultList
    }

    void updateJobResults(Long jobId, List<BulkableResult> bulkResults, List includes){
        List<Map> jsonResults = transformResults(bulkResults, includes as List)
        byte[] resultBytes = Jsonify.render(jsonResults).jsonText.bytes
        // FIXME dont do any, it spins back through the list to find the ok=false. spin through 1 time only.
        boolean ok = !(bulkResults.any({ it.ok == false}))
        jobRepo.update([id:jobId, ok: ok, results: resultBytes, state: JobState.Finished], [flush: true])
    }

    /**
     * Processes Results of bulkcreate and processes list of maps which can be converted to json and set on job.results
     *
     * @param results List<Results?
     * @param includes - List of fields to include in json response for successful records.
     *        this is `bulk` fields configured in restapi-config.yml and passed down by RestRepositoryApi
     * @return //FIXME #339 codenarxc will fail on empty return docs
     */
    private List<Map> transformResults(List<BulkableResult> results, List includes = []) {
        List<Map> ret = []
        for (BulkableResult r : results) {
            Map<String, Object> m
            if (r.ok) {
                //successful result would have entity, use the includes list to prepare result object
                m =  [id: r.entity['id'], ok: true] as Map<String, Object>
                if(includes) m << entityMapService.createEntityMap(r.entity, includes) as Map<String, Object>
            } else {
                //failed result would have original incoming map, return it as it is
                m = [ok: false] as Map<String, Object>
                m["data"] = r.requestData //set original incoming data map
                m["error"] = r.error
            }
            ret << m
        }
        return ret
    }
}
