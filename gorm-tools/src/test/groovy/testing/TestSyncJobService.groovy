package testing

import groovy.transform.CompileStatic

import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import yakworks.api.ApiResults
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate

@CompileStatic
class TestSyncJobService implements SyncJobService {

    /**
     * create Job and returns the job id
     */
    @Override
    Long createJob(String source, String sourceId, Object payload) {
        Validate.notNull(payload)
        byte[] reqData = JsonEngine.toJson(payload).bytes
        Map data = [source: source, sourceId: sourceId, state: SyncJobState.Running, requestData: reqData]
        def job = TestSyncJob.repo.create((Map)data, (Map)[flush:true])

        return job.id
    }

    @Override
    void updateJob(Long id, SyncJobState state, ApiResults results, List<Map> renderResults) {
        byte[] resultBytes = JsonEngine.toJson(renderResults).bytes
        Map data = [id:id, ok: results.ok, data: resultBytes, state: state]
        TestSyncJob.repo.update((Map)data, (Map)[flush: true])
    }

    SyncJobEntity getJob(Serializable id){
        TestSyncJob.get(id)
    }

}
