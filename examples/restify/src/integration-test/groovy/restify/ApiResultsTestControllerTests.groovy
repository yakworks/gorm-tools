package restify

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.http.RestIntTest
import yakworks.gorm.testing.http.RestIntegrationTest

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
