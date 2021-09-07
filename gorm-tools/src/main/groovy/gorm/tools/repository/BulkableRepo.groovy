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

import gorm.tools.async.AsyncSupport
import gorm.tools.job.JobRepoTrait
import gorm.tools.job.JobTrait
import gorm.tools.json.Jsonify
import gorm.tools.support.Results

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

    @Value('${hibernate.jdbc.batch_size:50}')
    int batchSize  //XXX https://github.com/9ci/domain9/issues/331  test if @Value works on trait

    @Value('${nine.autocash.parallelProcessing.enabled:false}')
    boolean parallelProcessingEnabled //XXX https://github.com/9ci/domain9/issues/331  we might have to move it, here we can have default

    // GormRepo implements it
    abstract void bindAndCreate(D entity, Map data, Map args)

    // need cleaner way to do transaction, change it
    abstract gormStaticApi()
    abstract  Class<D> getEntityClass()

    //// XXX https://github.com/9ci/domain9/issues/331 Not sure if needed any more
    //abstract List bulkCreate()

//////// MOVED FROM GORM REPO

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

        // XXX We can do something similar for error count
        AtomicInteger count = new AtomicInteger(0)

        // for error handling -- based on `onError` from args commit success and report the failure or fail them all
        //  also use errorThreshold, for example if it failed on more than 10 records we stop and rollback everything
        //@jdabal - for parallel async, we can not rollback batches which are already commited
        //as each batch would be in its own transaction.
        List bulkResult = []
        if(parallelProcessingEnabled) {
            //run the batch parallel in batched transactions
            asyncSupport.parallel([batchSize:batchSize], dataList.collate(batchSize)) { List<Map> batch ->
                // returns entity that was created, but into Job object we only want id and sourceid.
                // id:123, source.sourceId, -  @jdabal assume that every domain which is bulk imported haas sourceId field ?
                // store results in resultList,but id and sourceId only for succesfully created records
                List results = doBulkCreate(batch, args)
                if(results) bulkResult.addAll transformResults(results)
            }
        } else {
            List results = doBulkCreate(dataList, args)
            if(results) bulkResult.addAll transformResults(results)
        }

        count.getAndAdd(bulkResult.size())

        job['results'] = Jsonify.render(bulkResult).jsonText.bytes
        job.persist()

        //XXX  https://github.com/9ci/domain9/issues/331 assign jobId on each record created.
        // Special handling for arTran - we will have ArTranJob (jobId, arTranId). For all others we would
        //have JobLink (jobId,entityId, entityName)
        return job
    }

    List<Results> doBulkCreate(List<Map> dataList, Map args = [:]){
        List<Results> resultList = [] as List<Results>
        for (Map item : dataList) {
            D entity = (D)getEntityClass().newInstance()
            Results r
            try {
                //need to do bindAndCreate - so that even if create fails, we can get source.sourceId if it is created
                bindAndCreate(entity, item, args)
                r = Results.OK().id(entity["id"] as Long)
            } catch(Exception e) {
                r = Results.error(e)
            }
            String sourceId = getSourceId(entity, item)
            if(sourceId) {
                r.meta['sourceId'] = sourceId
            }
            resultList.add r
        }
        return resultList
    }

    private String getSourceId(D entity, Map item) {
        String sourceId = null
        //if the domain has source.sourceId return it
        if(entity && entity.hasProperty("source") && entity['source']) {
            sourceId = entity["source"]["sourceId"]
        }
        return sourceId
    }

    private List<Map> transformResults(List<Results> results) {
        List<Map> ret = []
        for (Results r : results) {
            Map m = [:]
            if (r.ok) {
                m =  [id: r.id, success: "true"]
            } else {
                m = [success: "false", message: r.message]
            }
            if(r.meta["sourceId"]) m['sourceId'] = r.meta["sourceId"] as String
            ret << m

        }
        return ret
    }



    // Class<J> jobClass // the domain class this is for
    // /**
    //  * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
    //  */
    // @Override
    // Class<J> getJobClass() {
    //     if (!jobClass) this.jobClass = (Class<J>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo)
    //     return jobClass
    // }


}
