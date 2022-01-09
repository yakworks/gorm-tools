/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import java.nio.file.Path

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import yakworks.api.ApiResults

@CompileStatic
trait SyncJobService<D> {

    /**
     * creates Job using the repo and returns the jobId
     */
    abstract GormRepo<D> getJobRepo()

    /**
     * creates and saves the Job and returns the SyncJobContext with the jobId
     */
    SyncJobContext createJob(SyncJobArgs args, Object payload){
        def sjc = new SyncJobContext(args: args, syncJobRepo: getJobRepo(), payload: payload )
        return sjc.createJob()
    }

    /**
     * gets the job from the repo
     */
    SyncJobEntity getJob(Serializable id){
        return getJobRepo().get(id) as SyncJobEntity
    }

    /**
     * Creates a nio path file for the id passed in.
     * Will be "${tempDir}/SyncJobData${id}.json".
     * For large bulk operations data results should be stored as attachment file
     *
     * @param id the job id
     * @return the Path object to use
     */
    abstract Path createTempFile(Serializable id)

}
