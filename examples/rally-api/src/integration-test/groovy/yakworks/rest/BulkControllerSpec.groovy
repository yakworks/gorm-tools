package yakworks.rest

import yakworks.rally.api.SpringApplication
import yakworks.rest.gorm.controller.CrudApiController
import grails.testing.mixin.integration.Integration
import org.apache.commons.lang3.StringUtils
import yakworks.testing.rest.RestIntTest
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Org

import static org.springframework.http.HttpStatus.MULTI_STATUS

// @Rollback
@Integration(applicationClass = SpringApplication)
class BulkControllerSpec extends RestIntTest {

    CrudApiController<Org> controller

    void setup() {
        controllerName = 'OrgController'
    }

    void "verify bulk create and sanity check response"() {
        given:
        List<Map> jsonList = [
            [num: "x00f1", name: "Foox1", type: "Customer", location: [ "street1": "string",  "street2": "string", "city": "string"]],
            [num: "x00f2", name: "Foox2", type: "Customer"],
            [num: "x00f3", name: "Foox3", type: "Customer"],
        ]

        when:
        request.json = jsonList
        request.method = "POST"
        //sourceId is built from these so pop to test
        request.requestURI = '/api/rally/org/bulk'
        request.queryString = 'jobSource=Oracle'

        controller.params.jobSource = "Oracle"
        controller.bulkCreate()
        Map body = response.bodyToMap()

        then:
        body.id != null
        body.ok == true
        body.state == "Finished"
        body.source == "Oracle" //should have been picked from query string
        // body.sourceId == "POST /api/rally/org/bulk?jobSource=Oracle"
        body.data != null
        body.data.size() == 3
        body.data[0].data.id != null
        // body.status == MULTI_STATUS.value()
        response.status == MULTI_STATUS.value()

    }

    void "test failures should rollback"() {
        List<Map> jsonList = [[num: "foox2", name: "Foox2", type: "Customer"]]
        jsonList[0].num = StringUtils.rightPad("ORG-1-", 110, "X")

        when:
        request.json = jsonList
        request.method = "POST"
        request.requestURI = '/api/rally/org/bulk'
        request.queryString = 'jobSource=Oracle'
        controller.params.jobSource = "Oracle"
        controller.bulkCreate()
        Map body = response.bodyToMap()

        then:
        noExceptionThrown()
        body.id

        when:
        SyncJob job = SyncJob.repo.getWithTrx(body.id as Long)

        then:
        job.id
        job.data != null
    }

}
