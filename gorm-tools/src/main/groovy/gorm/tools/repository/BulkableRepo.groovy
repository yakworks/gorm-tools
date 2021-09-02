/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.async.AsyncSupport
import gorm.tools.job.JobRepoTrait
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.GenericTypeResolver

import gorm.tools.job.JobTrait
import org.springframework.transaction.TransactionStatus

import java.util.concurrent.atomic.AtomicInteger

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@CompileStatic
trait BulkableRepo<D, J extends JobTrait>  {

    @Autowired(required = false)
    JobRepoTrait jobRepo

    @Autowired
    AsyncSupport asyncSupport

    // @Value('${hibernate.jdbc.batch_size:50}')
    int batchSize  //XXX https://github.com/9ci/domain9/issues/331  test if @Value works on trait

    // @Value('${nine.autocash.parallelProcessing.enabled:false}')
    boolean parallelProcessingEnabled //XXX https://github.com/9ci/domain9/issues/331  we might have to move it, here we can have default

    // GormRepo implements it
    abstract D doCreate(Map data, Map args)

    // need cleaner way to do transaction, change it
    abstract gormStaticApi()

    //// XXX https://github.com/9ci/domain9/issues/331 Not sure if needed any more
    abstract List bulkCreate()

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

        // XXX https://github.com/9ci/domain9/issues/331 call to create Job
        // create job
        J job

        // XXX We can do something similar for error count
        AtomicInteger count = new AtomicInteger(0)

        // for error handling -- based on `onError` from args commit success and report the failure or fail them all
        //  also use errorThreshold, for example if it failed on more than 10 records we stop and rollback everything

        // @Sudhir - I took below code from domain9 FinanceChargeService in case you need reference
        if(parallelProcessingEnabled) {
            //run the batch parallel in batched transactions
            asyncSupport.parallelCollate([batchSize:batchSize], dataList) { Map row ->
                // returns entity that was created, but into Job object we only want id and sourceid.
                // id:123, source.sourceId
                // we need a method that takes List resultList that was returned and does only subset and returns List<Map> and puts in job

                // store results in resultList,but id and sourceId only for succesfully created records
                def results = doBulkCreate(dataList, args)
                count.getAndIncrement()
            }
        } else {
            for(Map row : dataList) {
                doBulkCreate(dataList, args)
                count.getAndIncrement()
            }
        }

        //XXX https://github.com/9ci/domain9/issues/331 when completed assign results on Job.results

        //XXX  https://github.com/9ci/domain9/issues/331 assign jobId on each record created.
        // Special handling for arTran - we will have ArTranJob (jobId, arTranId). For all others we would
        //have JobLink (jobId,entityId, entityName)
        return job
    }

    List<D> doBulkCreate(List<Map> dataList, Map args = [:]){
        List resultList = [] as List<D>
        for (Map item : dataList) {
            // wrap in try/catch
            // put error in results object, just like in cash app (for example CorrectService.correctPaymentList) and we can move on if one failed
            D entity = doCreate(item, args)
            resultList.add(entity)
        }
        return resultList
    }

//////////////////

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
