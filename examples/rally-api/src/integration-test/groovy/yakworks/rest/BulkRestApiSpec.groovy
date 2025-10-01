package yakworks.rest

import grails.gorm.transactions.Rollback
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus

import gorm.tools.job.SyncJobState
import yakworks.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource

import static yakworks.json.groovy.JsonEngine.parseJson

@Integration
@Rollback
class BulkRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/org/bulk?jobSource=Oracle&async=false"

    def setup(){
        login()
    }

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
        body.sourceId == "POST /api/rally/org/bulk?jobSource=Oracle&async=false"
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
        job
        job.dataToString()
        // we no longer have settign to disable payload saving
        //job.payloadBytes == null
        job.state == SyncJobState.Finished
        job.sourceId == "POST /api/rally/org/bulk?jobSource=Oracle&async=false"
        job.source == "Oracle"

        /*payload disabled
        when: "Verify job.data json, this is what come in from the request"
        List dataList = parseJson(job.payloadToString())

        then:
        dataList.size() == 3
        dataList[0].num == "foox1"
         */

        when: "Verify created org"
        Org org = Org.repo.get( body.data[0].data.id as Long)

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
        job.dataToString()

        when:
        List json = job.dataList
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
        jsonList[0].num = StringUtils.rightPad("ORG-1-", 110, "Z")

        when:
        Response resp = post(path, jsonList)
        Map body = bodyToMap(resp)
        SyncJob job = SyncJob.repo.getWithTrx(body.id as Long)

        then:
        body.ok == false
        body.state == 'Finished'
        job.id
        job.dataToString()

        when:
        List json = job.dataList
        List requestData = job.payloadList

        then:
        json != null
        requestData != null
        json.size() == 2

        and: "Verify error fields"
        json[0].ok == false
        json[0].data instanceof Map
        json[0].data.size() == 2 //just two error fields - num and name
        json[0].data.num.startsWith "ORG-1-ZZZ"
        json[0].data.name ==  "Foox1"

        and: "Verify success fields"
        json[1].ok == true
        json[1].data instanceof Map
        json[1].data.size() == 4 //4 fields for success ful response - id, num, name and source.sourceId
        json[1].data.id !=  null
        json[1].data.num == "Foox2"
        json[1].data.name ==  "Foox2"
        json[1].data.source.sourceId ==  "Foox2"

        when: "verify problems are stored in problems field too for bulk"
        List problems = job.problems

        then: "should pickup problems from data"
        problems.size() == 1
        problems[0].payload.name == "Foox1"

        delete("/api/rally/org", json.data[1].id)
    }

    def "upsert"() {

        setup:
        int orgCount
        OrgSource.withTransaction {
            orgCount = Org.count()
            def list = Org.list()
            assert list
        }

        List<Map> jsonList = [
            // this should update based on num
            [num: "91", name: "updated"],
            //this should update based on id
            [id: "92", name: "updated2"],
            //this should update based on sourceId
            [sourceId: "93", name: "updated3"],
            //this should be inserted
            [num: "fox1", name: "Fox1", type: "Customer"],
            //this should fail because it doesn't have bindId
            [id: 999999, num: "fox2", name: "Fox2", type: "Customer"]
        ]

        when:
        Response resp = post("$path&op=upsert", jsonList)

        Map body = bodyToMap(resp)

        then: "sanity check json structure"
        body.id != null
        body.ok == false //has one failure
        body.state == "Finished"
        body.source == "Oracle" //should have been picked from query string
        body.sourceId == "POST /api/rally/org/bulk?jobSource=Oracle&async=false&op=upsert"
        resp.code() == HttpStatus.MULTI_STATUS.value()
        body.data.size() == 5

        when:
        List data = body.data
        int success = data.count { it.ok}
        int failed = data.count { !it.ok}
        int inserted = data.count { it.status == 201}
        int updated = data.count { it.status == 200}

        then:
        success == 4
        failed == 1
        inserted == 1
        updated == 3
    }

    void "bulk export"() {
        when:
        Response resp = get("/api/rally/org/bulk?q=*&includesKey=uiGET")
        Map body = bodyToMap(resp)

        then:
        body
        body.id
        body.state == SyncJobState.Queued.name()
        body.sourceId

        //and: "verify job has been put in queue"
        //syncJobQueue.size() == 1
    }
}
