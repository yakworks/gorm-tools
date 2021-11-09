/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.job.RepoSyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.repository.bulk.BulkableResults
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate

@Lazy @Service('repoJobService')
@CompileStatic
class DefaultRepoSyncJobService implements RepoSyncJobService {

    @Autowired
    SyncJobRepo jobRepo

    /**
     * create Job and returns the job id
     */
    @Override
    Long createJob(String source, String sourceId, Object payload) {
        Validate.notNull(payload)
        byte[] reqData = JsonEngine.toJson(payload).bytes
        Map data = [source: source, sourceId: sourceId, state: SyncJobState.Running, requestData: reqData]
        def job = jobRepo.create((Map)data, (Map)[flush:true])
        return job.id
    }

    @Override
    void updateJob(Long id, SyncJobState state, BulkableResults results, List<Map> renderResults) {
        byte[] resultBytes = JsonEngine.toJson(renderResults).bytes
        Map data = [id:id, ok: results.ok, data: resultBytes, state: state]
        jobRepo.update((Map)data, (Map)[flush: true])
    }

    SyncJob getJob(Serializable id){
        jobRepo.get(id)
    }

}
