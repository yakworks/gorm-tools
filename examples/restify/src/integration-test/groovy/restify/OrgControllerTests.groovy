package restify

import gorm.tools.rest.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.commons.map.Maps
import yakworks.gorm.testing.http.RestIntegrationTest
import yakworks.rally.orgs.model.Org

@Rollback
@Integration
class OrgControllerTests extends Specification implements RestIntegrationTest {

    RestRepoApiController<Org> controller

    void setup() {
        controllerName = 'OrgController'
    }

    void "get with id"() {
        when:
        controller.params.id = 9
        controller.get()
        Map body = response.bodyToMap()

        then:
        response.status == 200
        Maps.containsAll(body, [id:9, num: '9', name: 'Org9'])
    }

    void "post with empty data"() {
        when:
        request.json = [:]
        controller.post()
        Map body = response.bodyToMap()

        then:
        body.status == 422
        response.status == 422
        body.detail.title == 'Empty Data'
    }

    void "post with no orgType"() {
        when:
        request.json = [foo: 'bar']
        controller.post()
        Map body = response.bodyToMap()

        then:
        body.status == 422
        response.status == 422
        body.detail == '[org.type] must not be null'
    }
}
