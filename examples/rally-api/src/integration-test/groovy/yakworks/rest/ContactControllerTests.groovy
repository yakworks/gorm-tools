package yakworks.rest

import yakworks.gorm.rest.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.commons.map.Maps
import yakworks.testing.http.RestIntTest
import yakworks.rally.orgs.model.Contact

@Rollback
@Integration
class ContactControllerTests extends RestIntTest {

    RestRepoApiController<Contact> controller
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
