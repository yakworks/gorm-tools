package testing

import groovy.transform.CompileStatic

import gorm.tools.job.RepoSyncJobEntity
import gorm.tools.job.RepoSyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.json.Jsonify
import gorm.tools.repository.bulk.BulkableResults
import yakworks.commons.lang.Validate

@CompileStatic
class TestRepoSyncJobService implements RepoSyncJobService {

    /**
     * create Job and returns the job id
     */
    @Override
    Long createJob(String source, String sourceId, Object payload) {
        Validate.notNull(payload)
        byte[] reqData = Jsonify.render(payload).jsonText.bytes
        Map data = [source: source, sourceId: sourceId, state: SyncJobState.Running, requestData: reqData]
        def job = TestRepoSyncJob.repo.create((Map)data, (Map)[flush:true])
        return job.id
    }

    @Override
    void updateJob(Long id, SyncJobState state, BulkableResults results, List<Map> renderResults) {
        byte[] resultBytes = Jsonify.render(renderResults).jsonText.bytes
        Map data = [id:id, ok: results.ok, data: resultBytes, state: state]
        TestRepoSyncJob.repo.update((Map)data, (Map)[flush: true])
    }

    RepoSyncJobEntity getJob(Serializable id){
        TestRepoSyncJob.get(id)
    }

}
