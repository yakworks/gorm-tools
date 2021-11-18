package testing

import groovy.transform.CompileStatic

import gorm.tools.job.RepoSyncJobEntity
import gorm.tools.job.RepoSyncJobService
import gorm.tools.job.SyncJobState

import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate

@CompileStatic
class TestRepoSyncJobService implements RepoSyncJobService {

    /**
     * create Job and returns the job id
     */
    @Override
    Long createJob(String source, String sourceId, Object payload) {
        Validate.notNull(payload)
        byte[] reqData = JsonEngine.toJson(payload).bytes
        Map data = [source: source, sourceId: sourceId, state: SyncJobState.Running, requestData: reqData]
        def job = TestRepoSyncJob.repo.create((Map)data, (Map)[flush:true])

        return job.id
    }

    @Override
    void updateJob(Long id, SyncJobState state, ApiResults results, List<Map> renderResults) {
        byte[] resultBytes = JsonEngine.toJson(renderResults).bytes
        Map data = [id:id, ok: results.ok, data: resultBytes, state: state]
        TestRepoSyncJob.repo.update((Map)data, (Map)[flush: true])
    }

    RepoSyncJobEntity getJob(Serializable id){
        TestRepoSyncJob.get(id)
    }

}
