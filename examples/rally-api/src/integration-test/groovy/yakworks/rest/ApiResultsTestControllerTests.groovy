package yakworks.rest

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import yakworks.testing.rest.RestIntTest
import yakworks.rally.api.ApiResultsTestController

@Rollback
@Integration
class ApiResultsTestControllerTests extends RestIntTest {

    ApiResultsTestController controller

    void setup() {
        controllerName = 'ApiResultsTestController'
    }

    void "test get"() {
        when:
        controller.get()
        Map body = response.bodyToMap()

        then:
        body.ok == true
        body.status == 207
        //its not 207 by default anymore
        response.status == 200
    }

    void "test get with problems"() {
        when:
        controller.getWithProblems()
        Map body = response.bodyToMap()

        then:
        !body.ok
        body.status == 207
        body.title == "Unhandled Problem"
        //its not 207 by default anymore
        response.status == 200
    }

}
