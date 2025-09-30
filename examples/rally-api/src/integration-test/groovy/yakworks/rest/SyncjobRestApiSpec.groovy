package yakworks.rest

import gorm.tools.job.SyncJobState
import gorm.tools.model.SourceType
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.etl.DataMimeTypes
import yakworks.gorm.api.bulk.BulkImportJobArgs
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
        def resp = get(endpoint+"/${job.id}?includes=*")
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
        !body.dataBytes
        !body.payloadBytes

        cleanup:
        if(body && body.id) removeJob(body.id as Long)
    }

    void "test GET data"() {
        when:
        SyncJob job = createMockJob()
        def resp = get(endpoint+"/${job.id}/data")
        def data = bodyToMap(resp)

        then:
        data
        data.test == "value"

        cleanup:
        removeJob(job.id as Long)
    }

    void "ops not supported"() {
        when:
        def resp = post(endpoint, [sourceId:"123"])
        def custBody = bodyToMap(resp)

        then: "Verify cust tags created"
        resp.code() == 403
        custBody
        custBody.detail.contains "Syncjob does not support operation 'create'"

        when:
        resp = put(endpoint + "/1", [sourceId:"123"])
        custBody = bodyToMap(resp)

        then: "Verify cust tags created"
        resp.code() == 403
        custBody
        custBody.detail.contains "Syncjob does not support operation 'update'"

        when:
        resp = delete(endpoint + "/1")
        custBody = bodyToMap(resp)

        then: "Verify cust tags created"
        resp.code() == 403
        custBody
        custBody.detail.contains "Syncjob does not support operation 'delete'"
    }

    @Transactional
    SyncJob createMockJob() {
        SyncJob job = new SyncJob(
            sourceType: SourceType.ERP,
            sourceId: 'ar/org',
            jobType: BulkImportJobArgs.JOB_TYPE,
            dataFormat: DataMimeTypes.json
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
