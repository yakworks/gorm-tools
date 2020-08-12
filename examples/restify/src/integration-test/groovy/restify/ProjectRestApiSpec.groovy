package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class ProjectRestApiSpec extends RestApiFuncSpec {

    String path = "api/project"

    Map postData = [name: "project", num: "x123"]

    Map putData = [name: "project Update", num: "x123u"]

    Map invalidData = ["name": null]

}
