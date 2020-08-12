package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class BookRestApiSpec extends RestApiFuncSpec {
    String path = "api/book"
    Map postData = [title: "foo"]
    Map putData = [title: "updated foo"]

}
