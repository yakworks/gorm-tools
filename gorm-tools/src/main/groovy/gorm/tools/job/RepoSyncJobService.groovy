/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.repository.bulk.BulkableResults

@CompileStatic
interface RepoSyncJobService {

    /**
     * create Job and returns the job id
     */
    Long createJob(String source, String sourceId, Object payload)

    /**
     * update a job with state and results
     */
    void updateJob(Long id, SyncJobState state, BulkableResults results, List<Map> renderResults)

    RepoSyncJobEntity getJob(Serializable id)

}
