package yakworks

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.gorm.testing.http.RestIntTest
import yakworks.rally.api.ApiResultsTestController

@Rollback
@Integration
class ApiResultsTestControllerTests extends RestIntTest{

    ApiResultsTestController controller

    void setup() {
        controllerName = 'ApiResultsTestController'
    }

    void "get"() {
        when:
        controller.get()
        Map body = response.bodyToMap()

        then:
        body.ok == true
        body.status == 207
        response.status == 200

    }

}
