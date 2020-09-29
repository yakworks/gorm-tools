package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiTestTrait
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class ExampleUserRestApiSpec extends GebSpec implements RestApiTestTrait {

    String getResourcePath() {
        "${baseUrl}api/exampleUser"
    }

    void setup() {
        // this does not rollback
        new ExampleUser(userName: "project", magicCode: "x123").persist()
    }
    //data to force a post or patch failure
    Map getInvalidData() { [userName: null] }

    Map postData = [userName: "project", magicCode: "x123"]

    Map putData = [userName: "project Update", magicCode: "x123u"]

}
