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
        //its not 207 by default anymore
        // body.status == 207
        // response.status == 207
        body.status == 200
        response.status == 200

    }

}
