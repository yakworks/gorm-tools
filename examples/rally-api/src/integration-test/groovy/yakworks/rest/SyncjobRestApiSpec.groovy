package yakworks.rest

import gorm.tools.model.SourceType
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
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
        body.ok
        body.createdDate
        body.editedDate
        body.data
        body.data.test == "value"

        cleanup:
        if(body && body.id) removeJob(body.id as Long)
    }

    @Transactional
    SyncJob createMockJob() {
        SyncJob job = new SyncJob([sourceType: SourceType.ERP, sourceId: 'ar/org'])
        Map data = [test:"value"]
        job.dataBytes = JsonEngine.toJson(data).bytes
        job.ok = true
        return job.persist()
    }

    @Transactional
    void removeJob(def id) {
        SyncJob.repo.removeById(id)
    }
}
