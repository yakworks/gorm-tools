package restify

import geb.spock.GebSpec
import gorm.tools.rest.client.RestApiTestTrait
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class ProjectRestApiSpec extends GebSpec implements RestApiTestTrait {

    String path = "api/project"

    Map postData = [name: "project", num: "x123"]

    Map putData = [name: "project Update", num: "x123u"]

    Map invalidData = ["name": null]

    void "exercise api"() {
        expect:
        testGet()
        testPost()
        testPut()
        testDelete()
    }
}
