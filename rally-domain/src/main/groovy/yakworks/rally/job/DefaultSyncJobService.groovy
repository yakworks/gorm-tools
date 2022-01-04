/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import yakworks.api.ApiResults
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate

@Lazy @Service('syncJobService')
@CompileStatic
class DefaultSyncJobService implements SyncJobService {

    @Autowired
    SyncJobRepo syncJobRepo

    /**
     * create Job and returns the job id
     */
    @Override
    Long createJob(String source, String sourceId, Object payload) {
        Validate.notNull(payload)
        //XXX https://github.com/yakworks/gorm-tools/issues/426 don't assign requestData for now for testing large data
        //byte[] reqData = JsonEngine.toJson(payload).bytes
        byte[] reqData
        Map data = [source: source, sourceId: sourceId, state: SyncJobState.Running, requestData: reqData]
        def job = syncJobRepo.create((Map)data, (Map)[flush:true])
        return job.id
    }

    @Override
    void updateJob(Long id, SyncJobState state, ApiResults results, List<Map> renderResults) {
        //XX Handle exception during json conversion, so job.data and status updated even if json building fails.
        byte[] resultBytes = JsonEngine.toJson(renderResults).bytes
        Map data = [id:id, ok: results.ok, data: resultBytes, state: state]
        syncJobRepo.update((Map)data, (Map)[flush: true])
    }

    SyncJob getJob(Serializable id){
        syncJobRepo.get(id)
    }

}
