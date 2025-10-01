package yakworks.rest

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Ignore
import spock.lang.Specification

import yakworks.rally.orgs.model.Org
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

@Integration
class RestApiQueryValidationSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/org"

    void setup(){
        login()
    }

    void cleanupSpec() {
        OkAuth.TOKEN = null
    }

    @Ignore("FIXME @SUD, this user now doesnt have permission to read orgs")
    @Rollback
    void "test list - non admin user"() {
        setup: "this user cant max > 50"
        login("noroles", "123")

        when:
        Response resp = get("$path?q=*&max=60")
        Map body = bodyToMap(resp)

        then:
        !body.ok
        body.code == "error.query.max"
        body.title == "Max parameter value is larger then allowed value 50"

        when: "user supplied max is smaller thn query config max"
        resp = get("$path?q=*&max=5")
        body = bodyToMap(resp)

        then:
        body
        body.total == countPages(5)

        cleanup:
        OkAuth.TOKEN = null
    }

    @Rollback
    void "test list - admin user"() {
        setup: "this user can max upto 100"

        when: "max is good"
        def resp = get("$path?q=*&max=50")
        Map body = bodyToMap(resp)

        then:
        body
        body.total == countPages(50) //max=50 should have been applied

        when: "max is same as configured max"
        resp = get("$path?q=*&max=100")
        body = bodyToMap(resp)

        then:
        body
        body.total == countPages(100) //max=100 should have been applied

        when: "user supplied max is smaller thn configured default"
        resp = get("$path?q=*&max=10")
        body = bodyToMap(resp)

        then:
        body
        body.total == countPages(10) //max=10 should have been applied

        when: "max is > user specific max"
        resp = get("$path?q=*&max=101")
        body = bodyToMap(resp)

        then:
        !body.ok
        body.code == "error.query.max"
        body.title == "Max parameter value is larger then allowed value 100"
    }

    void "test timeout"() {
        when:
        Response resp = get("$path?q={timeout:true}")
        Map body = bodyToMap(resp)

        then:
        !body.ok
        body.code == "error.query.timeout"
        body.title == "Query timeout has occurred"
    }

    void "list with format xlsx"() {
        when:
        def resp = get("$path?q=*&max=1000&format=xlsx")

        then: "should have allowed it"
        resp.code == 200
        resp.header('Content-Disposition') == 'attachment;filename="org.xlsx"'
    }

    //returns the number of pages for org, based on given max
    int countPages(int max) {
        return Math.ceil((Double) (Org.count() / max)).intValue()
    }
}
