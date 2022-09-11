package yakworks.rest

import yakworks.rest.gorm.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.commons.map.Maps
import yakworks.testing.rest.RestIntTest
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag

@Rollback
@Integration
class OrgControllerTests extends RestIntTest {

    RestRepoApiController<Org> controller

    void setup() {
        controllerName = 'OrgController'
    }

    void "is controller name working and does it have config"() {
        expect:
        controller.getControllerName() == 'org'
        // controller.getFieldIncludes(['get']) == ['*', 'info.*', "location.id", 'tags', 'contact.$*', 'contact.flex.num1']
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

    void "list CSV"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [format:'csv']
        controller.list()
        // Map body = response.bodyToMap()
        // List data = body.data

        then:
        response.status == 200
        response.header("Content-Type").contains("text/csv")
        response.header("Content-Disposition").contains('attachment;filename=')
        response.contentAsString

    }

    void "sanity check XLSX"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [format:'xlsx']
        controller.list()
        // Map body = response.bodyToMap()
        // List data = body.data

        then:
        response.status == 200
        response.header("Content-Type").contains("spreadsheetml")
        response.header("Content-Disposition").contains('attachment;filename=')
    }

    void "list with includes"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [includes:"id,num"]
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        response.status == 200
        (data[0] as Map).keySet().size() ==2
    }

    void "list with includesKey"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [includesKey:'bulk']
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        response.status == 200
        (data[0] as Map).keySet().size() == 4
    }

}
