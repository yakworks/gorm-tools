/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import java.util.concurrent.atomic.AtomicInteger

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.GenericTypeResolver
import org.springframework.validation.Errors

import gorm.tools.async.AsyncSupport
import gorm.tools.beans.EntityMapService
import gorm.tools.job.JobRepoTrait
import gorm.tools.job.JobTrait
import gorm.tools.json.Jsonify
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.support.Results
import grails.validation.ValidationException

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

    @Value('${hibernate.jdbc.batch_size:50}')
    int batchSize

    @Value('${nine.autocash.parallelProcessing.enabled:false}')
    boolean parallelProcessingEnabled

    //Here for @CompileStatic - GormRepo implements it
    abstract D doCreate(Map data, Map args)
    abstract  Class<D> getEntityClass()


    /**
     * allows to pass in bulk of records at once, for example /api/book/bulk
     * Each call creates a job that stores info for the call and is returned with results
     * @param dataList the list of data maps to create
     * @param args args to pass to doCreate. It can have:
     *      onError // 'rollback' or 'continue' (catches error) or 'skip'
     *      errorThreshold // number of errors to stop and rollback
     *      jdbc.batch_size // all, default or specify number
     *      gpars.poolsize
     * @return Job
     */
    J bulkCreate(List<Map> dataList, Map args = [:]){
        // create job
        Class<J> jobClass = (Class<J>)GenericTypeResolver.resolveTypeArguments(getClass(), BulkableRepo)[1]
        J job = jobClass.newInstance() as J

        // XXX error count / rollback support
        AtomicInteger count = new AtomicInteger(0)

        // for error handling -- based on `onError` from args commit success and report the failure or fail them all
        //  also use errorThreshold, for example if it failed on more than 10 records we stop and rollback everything
        //@jdabal - for parallel async, we can not rollback batches which are already commited
        //as each batch would be in its own transaction.
        List<Results> bulkResult = []
        try {
            if (getParallelProcessingEnabled()) {
                asyncSupport.parallel([batchSize: getBatchSize()], dataList.collate(getBatchSize())) { List<Map> batch ->
                    List<Results> results = doBulkCreate(batch, args)
                    if (results) bulkResult.addAll results
                }
            } else {
                List results = doBulkCreate(dataList, args)
                if (results) bulkResult.addAll results
            }
        } finally {
            count.getAndAdd(bulkResult.size())
            List<Map> jsonResults = transformResults(bulkResult, args.includes as List)
            job['results'] = Jsonify.render(jsonResults).jsonText.bytes
            job.ok = !(bulkResult.any({ it.ok == false}))
            job.persist()
        }


        //XXX  https://github.com/9ci/domain9/issues/331 assign jobId on each record created.
        // Special handling for arTran - we will have ArTranJob (jobId, arTranId). For all others we would
        //have JobLink (jobId,entityId, entityName)
        return job
    }

    List<Results> doBulkCreate(List<Map> dataList, Map args = [:]){
        List<Results> resultList = [] as List<Results>
        for (Map item : dataList) {
            //need to copy the incoming map, as during create(), repos may remove entries from the data map
            Map itmCopy = [:] << item  //Maps.deepCopy(item) doesnt work, as clone returns false
            D entityInstance
            Results r
            try {
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

    private List<Map> transformResults(List<Results> results, List includes = []) {
        List<Map> ret = []
        for (Results r : results) {
            Map<String, Object> m
            if (r.ok) {
                //successful result would have entity, use the includes list to prepare result object
                m =  [id: r.id, ok: true] as Map<String, Object>
                if(includes) m << entityMapService.createEntityMap(r.entity, includes) as Map<String, Object>
            } else {
                //failed result would have original incoming map, return it as it is
                m = [ok: false] as Map<String, Object>
                m["item"] = r.meta["item"] //set original incoming data map

                Exception ex = r.ex
                if(ex instanceof EntityValidationException || ex instanceof ValidationException) {
                    m["errors"] = RepoExceptionSupport.toErrorList(ex["errors"] as Errors)
                } else {
                    m["error"] = r.message
                }

            }
            ret << m
        }
        return ret
    }
}
