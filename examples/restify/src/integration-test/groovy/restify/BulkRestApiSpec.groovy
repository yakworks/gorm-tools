package restify

import grails.gorm.transactions.Rollback
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus

import gorm.tools.job.SyncJobState
import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource

import static yakworks.commons.json.JsonEngine.parseJson

@Integration
@Rollback
class BulkRestApiSpec extends Specification implements OkHttpRestTrait {
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
        body.sourceId == "POST /api/rally/org/bulk?jobSource=Oracle"
        body.data != null
        body.data.size() == 3
        body.data[0].data.id != null
        resp.code() == HttpStatus.MULTI_STATUS.value()

        and: "verify the 'bulk' includes from restapi-config.xml"
        body.data[0].data.source.sourceId == "foox1"
        body.data[0].data.num == "foox1"
        body.data[0].data.name == "Foox1"

        when: "Verify job.data"
        SyncJob job = SyncJob.repo.get(body.id as Long)

        then:
        job != null
        job.data != null
        job.payloadBytes != null
        job.state == SyncJobState.Finished
        job.sourceId == "POST /api/rally/org/bulk?jobSource=Oracle"
        job.source == "Oracle"

        when: "Verify job.data json, this is what come in from the request"
        List dataList = parseJson(job.payloadToString())

        then:
        dataList.size() == 3
        dataList[0].num == "foox1"

        when: "Verify created org"
        Org org = Org.repo.read( body.data[0].data.id as Long)

        then:
        org != null
        org.num == "foox1"
        org.location != null
        org.location.street1 == "string"

        delete("/api/rally/org", body.data[0].id)
        delete("/api/rally/org", body.data[1].id)
        delete("/api/rally/org", body.data[2].id)
    }

    void "test failures should rollback"() {
        List<Map> jsonList =  [[num: "foox1", name: "Foox1", type: "Customer"]]
        jsonList[0].num = StringUtils.rightPad("ORG-1-", 110, "X")

        when:
        Response resp = post(path, jsonList)
        Map body = bodyToMap(resp)
        SyncJob job = SyncJob.repo.getWithTrx(body.id as Long)

        then:
        body.ok == false
        body.state == 'Finished'
        job.id
        job.data != null

        when:
        List json = parseJson(job.dataToString())
        List requestData = parseJson(job.payloadToString())

        then:
        json != null
        requestData != null

        and: "no dangling records committed"
        OrgSource.withTransaction {
            OrgSource.findBySourceIdLike("ORG-1%") == null
        }
    }
    void "one fails - verify success & error includes"() {
        List<Map> jsonList =  [[num: "foox1", name: "Foox1", type: "Customer"], [num: "Foox2", name: "Foox2", type: "Customer"]]
        jsonList[0].num = StringUtils.rightPad("ORG-1-", 110, "X")

        when:
        Response resp = post(path, jsonList)
        Map body = bodyToMap(resp)
        SyncJob job = SyncJob.repo.getWithTrx(body.id as Long)

        then:
        body.ok == false
        body.state == 'Finished'
        job.id
        job.data != null

        when:
        List json = parseJson(job.dataToString())
        List requestData = parseJson(job.payloadToString())

        then:
        json != null
        requestData != null
        json.size() == 2

        and: "Verify error fields"
        json[0].ok == false
        json[0].data instanceof Map
        json[0].data.size() == 2 //just two error fields - num and name
        json[0].data.num.startsWith "ORG-1-XXX"
        json[0].data.name ==  "Foox1"

        and: "Verify success fields"
        json[1].ok == true
        json[1].data instanceof Map
        json[1].data.size() == 4 //4 fields for success ful response - id, num, name and source.sourceId
        json[1].data.id !=  null
        json[1].data.num == "Foox2"
        json[1].data.name ==  "Foox2"
        json[1].data.source.sourceId ==  "Foox2"

        delete("/api/rally/org", json.data[1].id)
    }
}
