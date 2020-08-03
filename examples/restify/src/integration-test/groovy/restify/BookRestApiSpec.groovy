package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class BookRestApiSpec extends RestApiFuncSpec {

    Class<Book> domainClass = Book
    boolean vndHeaderOnError = false

    String getResourcePath() {
        "${baseUrl}api/book"
    }

    //data to force a post or patch failure
    Map getInvalidData() { [title: null] }

    Map postData = [title: "project"]

    Map putData = [title: "project Update"]


}
