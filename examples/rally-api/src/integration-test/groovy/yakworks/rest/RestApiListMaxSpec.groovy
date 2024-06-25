package yakworks.rest

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rest.client.OkAuth
import yakworks.rest.client.WebClientTrait

@Integration
class RestApiListMaxSpec extends Specification  implements WebClientTrait {

    String path = "/api/rally/org"

    void setup(){
        login()
    }

    @Rollback
    void "test list - non admin user"() {
        setup: "this user cant max > 20"
        login("noroles", "123")

        when:
        ResponseEntity resp = get("$path?q=*&max=50")
        Map body = resp.body

        then:
        body
        body.total == Math.ceil((Double) (Org.count() / 20)).intValue() //max=20 should have been applied

        cleanup:
        OkAuth.TOKEN = null
    }

    @Rollback
    void "test list - admin user"() {
        setup: "this user can max upto 100"

        when:
        ResponseEntity resp = get("$path?q=*&max=50")
        Map body = resp.body

        then:
        body
        body.total == Math.ceil((Double) (Org.count() / 50)).intValue() //2 pages, max=50 should have been applied
    }
}
