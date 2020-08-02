package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class AppUserRestApiSpec extends RestApiFuncSpec {

    Class<AppUser> domainClass = AppUser
    // set to true if you have configured the _error.gson or controller
    // to return application/vnd.error vs application/json
    boolean vndHeaderOnError = false

    String getResourcePath() {
        "${baseUrl}api/appUser"
    }

    //data to force a post or patch failure
    Map getInvalidData() { [userName: null] }

    Map postData = [userName: "project", magicCode: "x123"]

    Map putData = [userName: "project Update", magicCode: "x123u"]

}
