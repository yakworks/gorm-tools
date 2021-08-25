/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileStatic

import org.springframework.core.GenericTypeResolver

import gorm.tools.job.JobTrait

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@CompileStatic
trait Bulkable<J extends JobTrait>  {


    Class<J> jobClass // the domain class this is for
    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    @Override
    Class<J> getJobClass() {
        if (!jobClass) this.jobClass = (Class<J>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo)
        return jobClass
    }

    abstract List bulkCreate()

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
    J bulkInsert(List<Map> dataList, Map args = [:]) {
        //create Job
        J job = (J) getJobClass().newInstance()

        //chunk data into chunks of jdbc.batch_size, have transaction per chunk. Use poolsize for multi thread

        // for error handling -- based on `onError` from args commit success and report the failure or fail them all
        //  also use errorThreshold, for example if it failed on more than 10 records we stop and rollback everything

        //call bulkCreate(modified so we can do transaction per chunk and error handling) and store results in resultList
        job.resultList = bulkCreate(dataList, args)
        job.persist()

        //assign jobId on each record created. Special handling for arTran - we will have ArTranJob (jobId, arTranId). For all others we would
        //have JobLink (jobId,entityId, entityName)

        return job
    }




    // ???? would we have a separate one for updates? Nothing urgent now, we can refactor later
    J bulkUpdate(List<Map> dataList, Map args = [:]) {
        //create Job
        J job = (J) getEntityClass().newInstance()

        //call bulkCreate and store results in resultList
        job.resultList = bulkUpdate(dataList, args)
        job.persist()

        return job
    }

}
