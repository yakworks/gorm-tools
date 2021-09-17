/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.validation.Errors

import gorm.tools.async.AsyncSupport
import gorm.tools.beans.EntityMapService
import gorm.tools.job.JobRepoTrait
import gorm.tools.job.JobState
import gorm.tools.job.JobTrait
import gorm.tools.json.Jsonify
import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.support.Results
import gorm.tools.transaction.TrxService
import grails.async.Promise
import grails.validation.ValidationException
import yakworks.commons.map.Maps

import static grails.async.Promises.task

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

        List<Results> bulkResults = Collections.synchronizedList([]) as List<Results>

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
    // J bulkCreateOld(List<Map> dataList, Map args = [:]){
    //
    //     //FIXME #339
    //     J job = (J)jobRepo.create([source:args.source, state: JobState.Running, dataPayload:dataList])
    //     // for error handling -- based on `onError` from args commit success and report the failure or fail them all
    //     //  also use errorThreshold, for example if it failed on more than 10 records we stop and rollback everything
    //     //@jdabal - for parallel async, we can not rollback batches which are already commited
    //     //as each batch would be in its own transaction.
    //     //@suhdir, we should count the errors and stop once it hits errorThreshold, no rollback needed
    //
    //     //FIXME #339 we wait way to long to transformResults, do it as we go?
    //     // if we do it as we go then what the performance impact? maybe do it as we go in chunks?
    //     List<Results> bulkResult = []
    //     List<Map> jsonResults
    //     try {
    //         if (getParallelProcessingEnabled()) {
    //             asyncSupport.parallel([batchSize: getBatchSize()], dataList.collate(getBatchSize())) { List<Map> batch ->
    //                 //FIXME #339 no transaction?
    //                 List<Results> results = doBulkCreate(batch, args)
    //                 // if (results) jsonResults.addAll transformResults(results, args.includes as List)
    //             }
    //             //FIXME #339 if we do it as we go then what you have above runs risk of erroring now and rolling back trx
    //         } else {
    //             List results = doBulkCreate(dataList, args)
    //             //FIXME #339 not DRY, dont code dupe
    //             // if (results) jsonResults.addAll transformResults(results, args.includes as List)
    //             if (results) bulkResult.addAll results
    //         }
    //     } finally {
    //         // byte[] resultBytes = jsonResults ? Jsonify.render(jsonResults).jsonText.bytes : null
    //         // boolean  ok = !(jsonResults.any({ it.ok == false}))
    //         // job = (J)jobRepo.update([id:job.id, ok:ok, results: resultBytes, state: JobState.Finished])
    //
    //         jsonResults = transformResults(bulkResult, args.includes as List)
    //         byte[] resultBytes = Jsonify.render(jsonResults).jsonText.bytes
    //         boolean  ok = !(bulkResult.any({ it.ok == false}))
    //         job = (J)jobRepo.update([id:job.id, ok:ok, results: resultBytes, state: JobState.Finished])
    //     }
    //
    //     //XXX  https://github.com/9ci/domain9/issues/331 assign jobId on each record created.
    //     // Special handling for arTran - we will have ArTranJob (jobId, arTranId). For all others we would
    //     //have JobLink (jobId,entityId, entityName)
    //     return job
    // }

    List<Results> doBulkCreateTrx(List<Map> dataList, Map args = [:]){
        trxService.withTrx {
            doBulkCreate(dataList, args)
        }
    }

    List<Results> doBulkCreate(List<Map> dataList, Map args = [:]){
        List<Results> resultList = [] as List<Results>
        for (Map item : dataList) {
            //need to copy the incoming map, as during create(), repos may remove entries from the data map
            Map itmCopy
            D entityInstance
            Results r
            try {
                itmCopy = Maps.deepCopy(item)
                entityInstance = doCreate(item, args)
                r = Results.OK().id(entityInstance["id"] as Long)
                r.entity = entityInstance
            } catch(Exception e) {
                r = Results.error(e)
                r.ex = e
                r.meta["item"] = itmCopy   //set original data map on error
            }
            resultList.add r
        }
        return resultList
    }

    void updateJobResults(Long jobId, List<Results> bulkResults, List includes){
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
    private List<Map> transformResults(List<Results> results, List includes = []) {
        List<Map> ret = []
        for (Results r : results) {
            Map<String, Object> m
            if (r.ok) {
                //successful result would have entity, use the includes list to prepare result object
                m =  [id: r.id, ok: true] as Map<String, Object>
                if(includes) m << entityMapService.createEntityMap(r.entity, includes) as Map<String, Object>
            } else {
                //FIXME #339 can we not share/reuse the ApiError objects here so its consitent.
                // why do we need a special way ndt keep it consitent with what a get blows?
                //failed result would have original incoming map, return it as it is
                m = [ok: false] as Map<String, Object>
                m["data"] = r.meta["item"] //set original incoming data map

                Exception ex = r.ex
                if(ex instanceof EntityValidationException || ex instanceof ValidationException) {
                    Errors err = ex["errors"] as Errors
                    if(err && !(err instanceof EmptyErrors)) {
                        m["errors"] = RepoExceptionSupport.toErrorList(ex["errors"] as Errors)
                    } else {
                        //FIXME #339 concrete object should support this, this is hacky
                        //this is some other exception wrapped in validation exception
                        m["error"] = ex.cause?.message
                    }
                } else {
                    m["error"] = r.message
                }

            }
            ret << m
        }
        return ret
    }
}
