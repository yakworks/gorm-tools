package yakworks.rest

import gorm.tools.job.SyncJobState
import gorm.tools.model.SourceType
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.api.bulk.BulkImportJobParams
import yakworks.json.groovy.JsonEngine
import yakworks.rally.job.SyncJob
import yakworks.rest.client.OkHttpRestTrait

@Integration
class SyncjobRestApiSpec extends Specification implements OkHttpRestTrait {

    String endpoint = "/api/rally/syncJob"

    def setup(){
        login()
    }

    void "test GET"() {
        when:
        SyncJob job = createMockJob()
        def resp = get(endpoint+"/${job.id}")
        def body = bodyToMap(resp)

        then:
        body
        body.id
        !body.ok
        body.state == 'Finished'
        body.createdDate
        body.editedDate
        body.data
        body.data.test == "value"
        body.problems
        body.problems[0].ok == false
        body.problems[0].title == "error"

        cleanup:
        if(body && body.id) removeJob(body.id as Long)
    }

    @Transactional
    SyncJob createMockJob() {
        SyncJob job = new SyncJob(
            sourceType: SourceType.ERP, sourceId: 'ar/org', jobType: BulkImportJobParams.JOB_TYPE
        )
        Map data = [test:"value"]
        job.dataBytes = JsonEngine.toJson(data).bytes
        job.ok = false
        job.state = SyncJobState.Finished
        job.problems = [["ok":false,"title":"error"]]
        return job.persist()
    }

    @Transactional
    void removeJob(def id) {
        SyncJob.repo.removeById(id)
    }
}
