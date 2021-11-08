package restify

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.http.RestIntegrationTest
import yakworks.rally.tag.model.Tag

@Rollback
@Integration
class ResultsTestControllerTests extends Specification implements RestIntegrationTest {

    ResultsTestController controller

    void setup() {
        controllerName = 'ResultsTestController'
    }

    void "get"() {
        when:
        // controller.params.id = 9
        controller.get()
        Map body = response.bodyToMap()

        then:
        body.ok == true
        response.status == 200

    }

}
