package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiTestTrait
import gorm.tools.security.domain.AppUser
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class UserRestApiSpec extends GebSpec implements RestApiTestTrait {

    String path = "api/user"

    void "get index list"() {
        when:
        def pageMap = testList()
        def data = pageMap.data

        then:
        data.size() == 2
    }

}
