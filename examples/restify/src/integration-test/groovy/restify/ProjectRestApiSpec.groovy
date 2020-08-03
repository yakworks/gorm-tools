package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreRest

import static org.springframework.http.HttpStatus.OK

@Integration(applicationClass = Application)
@Rollback
class ProjectRestApiSpec extends RestApiFuncSpec {

    String getResourcePath() {
        "${baseUrl}api/project"
    }

    // insert
    Map postData = [name: "project", num: "x123"]

    // update
    Map putData = [name: "project Update", num: "x123u"]

    Map invalidData = ["name": null]

    @Override
    def cleanup() {}
}
