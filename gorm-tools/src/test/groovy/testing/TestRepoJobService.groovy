package testing

import groovy.transform.CompileStatic

import gorm.tools.job.RepoJobEntity
import gorm.tools.job.RepoJobService
import gorm.tools.job.JobState
import gorm.tools.repository.bulk.BulkableResults
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate

@CompileStatic
class TestRepoJobService implements RepoJobService {

    /**
     * create Job and returns the job id
     */
    @Override
    Long createJob(String source, String sourceId, Object payload) {
        Validate.notNull(payload)
        byte[] reqData = JsonEngine.toJson(payload).bytes
        Map data = [source: source, sourceId: sourceId, state: JobState.Running, requestData: reqData]
        def job = TestRepoJob.repo.create((Map)data, (Map)[flush:true])
        return job.id
    }

    @Override
    void updateJob(Long id, JobState state, BulkableResults results, List<Map> renderResults) {
        byte[] resultBytes = JsonEngine.toJson(renderResults).bytes
        Map data = [id:id, ok: results.ok, data: resultBytes, state: state]
        TestRepoJob.repo.update((Map)data, (Map)[flush: true])
    }

    RepoJobEntity getJob(Serializable id){
        TestRepoJob.get(id)
    }

}
