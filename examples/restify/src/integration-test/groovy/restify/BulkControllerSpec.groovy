package restify


import gorm.tools.rest.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.http.RestIntegrationTest
import yakworks.rally.orgs.model.Org

import static org.springframework.http.HttpStatus.MULTI_STATUS

// @Rollback
@Integration
class BulkControllerSpec extends Specification implements RestIntegrationTest {

    RestRepoApiController<Org> controller

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

}
