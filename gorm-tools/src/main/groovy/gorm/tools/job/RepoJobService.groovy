/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import yakworks.api.ApiResults

@CompileStatic
interface RepoJobService {

    /**
     * create Job and returns the job id
     */
    Long createJob(String source, String sourceId, Object payload)

    /**
     * update a job with state and results
     */
    void updateJob(Long id, JobState state, ApiResults results, List<Map> renderResults)

    RepoJobEntity getJob(Serializable id)

}
