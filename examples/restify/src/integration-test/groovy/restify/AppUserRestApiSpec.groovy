package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class AppUserRestApiSpec extends RestApiFuncSpec {

    String getResourcePath() {
        "${baseUrl}api/appUser"
    }

    void setup() {
        // this does not rollback
        new AppUser(userName: "project", magicCode: "x123").persist()
    }
    //data to force a post or patch failure
    Map getInvalidData() { [userName: null] }

    Map postData = [userName: "project", magicCode: "x123"]

    Map putData = [userName: "project Update", magicCode: "x123u"]

}
