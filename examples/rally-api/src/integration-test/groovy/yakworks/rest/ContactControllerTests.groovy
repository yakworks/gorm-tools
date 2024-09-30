package yakworks.rest


import yakworks.rest.gorm.controller.CrudApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.commons.map.Maps
import yakworks.testing.rest.RestIntTest
import yakworks.rally.orgs.model.Contact

@Rollback
@Integration
class ContactControllerTests extends RestIntTest {

    CrudApiController<Contact> controller
    // String controllerName = 'ContactController'

    void setup() {
        controllerName = 'ContactController'
    }

    void "get with id"() {
        when:
        controller.params.id = 50
        controller.get()
        Map body = response.bodyToMap()

        then:
        response.status == 200
        Maps.containsAll(body, [id:50])
    }


    void "POST"() {
        when:
        request.JSON = [org:[id:2], num: "TEST1", name:"TEST1"]
        controller.post()
        Map body = response.bodyToMap()

        then:
        body
        body.name == "TEST1"

        and:
        Contact.findWhere(num:"TEST1")
    }

    void "put"() {
        setup:
        request.json = [id:1, firstName: "Updated"]
        controller.params.id = 1

        when:
        controller.put()
        Map body = response.bodyToMap()

        then:
        noExceptionThrown()
        body.id == 1
        body.firstName == "Updated"

        and:
        Contact.get(1).firstName == "Updated"
    }

    void "pick list"() {
        controller.params << [q:"John100", max:20]

        when:
        controller.picklist()
        Map body = response.bodyToMap()

        then:
        body.records == 1
        body.data[0].name.contains "John100"

    }

    void "list"() {
        when:
        controller.params << [q:"*", max:20]
        controller.list()
        Map body = response.bodyToMap()

        then:
        response.status == 200
        body.page == 1
        body.data.size() == 20
    }

    //
    // void "test post with tags"() {
    //     when: "Create a test tag"
    //     Tag tag1 = Tag.create(code: 'tagTest', entityName: 'Contact')
    //
    //     then:
    //     tag1
    //
    //     when: "Create customer with tags"
    //     request.json = [num:"tagTest", name:"tagTest", tags:[[id:tag1.id]]]
    //     controller.post()
    //     Map body = response.bodyToMap()
    //
    //     then: "Verify cust tags created"
    //     body.status != 422
    //     response.status == 201
    //     body.tags.size() == 1
    //     body.tags[0].id == tag1.id
    // }
}
