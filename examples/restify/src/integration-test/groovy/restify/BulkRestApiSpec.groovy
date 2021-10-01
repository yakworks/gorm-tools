package restify

import gorm.tools.job.JobState
import gorm.tools.rest.JsonParserTrait
import gorm.tools.rest.client.OkHttpRestTrait
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Org

@Rollback
@Integration
class BulkRestApiSpec extends Specification implements OkHttpRestTrait, JsonParserTrait {
    String path = "/api/rally/org/bulk?jobSource=Oracle"

    void "verify bulk create and sanity check response"() {
        given:
        List<Map> jsonList = [
            [num: "foox1", name: "Foox1", type: "Customer", location: [ "street1": "string",  "street2": "string", "city": "string"]],
            [num: "foox2", name: "Foox2", type: "Customer"],
            [num: "foox3", name: "Foox3", type: "Customer"],
        ]
        when:
        Response resp = post(path, jsonList)

        Map body = bodyToMap(resp)

        then: "sanity check json structure"
        body.id != null
        body.ok == true
        body.state == "Finished"
        body.source == "Oracle" //should have been picked from query string
        body.sourceId == "org/bulkCreate"
        body.data != null
        body.data.size() == 3
        body.data[0].data.id != null
        resp.code() == HttpStatus.CREATED.value()

        and: "verify the 'bulk' includes from restapi-config.xml"
        body.data[0].data.source.sourceId == "foox1"
        body.data[0].data.num == "foox1"
        body.data[0].data.name == "Foox1"

        when: "Verify job.data"
        Job job = Job.get(body.id as Long)

        then:
        job != null
        job.data != null
        job.requestData != null
        job.state == JobState.Finished
        job.sourceId == "org/bulkCreate"
        job.source == "Oracle"

        when: "Verify job.data json, this is what come in from the request"
        List dataList = parseJson(job.requestData)

        then:
        dataList.size() == 3
        dataList[0].num == "foox1"

        when: "Verify created org"
        Org org = Org.get( body.data[0].data.id as Long)

        then:
        org != null
        org.num == "foox1"
        org.location != null
        org.location.street1 == "string"

        delete("/api/rally/org", body.data[0].id)
        delete("/api/rally/org", body.data[1].id)
        delete("/api/rally/org", body.data[2].id)
    }
}
