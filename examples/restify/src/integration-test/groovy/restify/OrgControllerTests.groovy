package restify

import gorm.tools.rest.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.commons.map.Maps
import yakworks.gorm.testing.http.RestIntegrationTest
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag

@Rollback
@Integration
class OrgControllerTests extends Specification implements RestIntegrationTest {

    RestRepoApiController<Org> controller

    void setup() {
        controllerName = 'OrgController'
    }

    void "is controller name working and does it have config"() {
        expect:
        controller.getControllerName() == 'org'
        controller.getFieldIncludes(['get']) == ['*', 'info.*', "location.id", 'tags']
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
        body.status == 400
        response.status == 400
        body.title == 'Empty Data'
    }

    void "post with no orgType"() {
        when:
        request.json = [foo: 'bar']
        controller.post()
        Map body = response.bodyToMap()

        then:
        body.status == 422
        response.status == 422
        body.title == 'Org Validation Error(s)'
    }

    void "test post with tags"() {
        when: "Create a test tag"
        Tag tag1 = Tag.create(code: 'tagTest', entityName: 'Customer')

        then:
        tag1

        when: "Create customer with tags"
        request.json = [num:"tagTest", name:"tagTest", type: 'Customer', tags:[[id:tag1.id]]]
        controller.post()
        Map body = response.bodyToMap()

        then: "Verify cust tags created"
        body.status != 422
        response.status == 201
        body.tags.size() == 1
        body.tags[0].id == tag1.id
    }

    void "list sort"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [max:20, sort:'contact.flex.num1', order:'desc']
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        response.status == 200
        data[0].contact.flex.num1 > data[1].contact.flex.num1
    }

}
