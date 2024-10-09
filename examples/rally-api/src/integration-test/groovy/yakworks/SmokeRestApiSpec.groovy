package yakworks

import org.springframework.http.HttpStatus

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.rest.client.OkHttpRestTrait

@Integration
class SmokeRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally"

    def setup(){
        login()
    }

    void "get smoke test"() {
        when:
        Response resp = get("$path/smoke?foo=bar")

        then:
        resp.code() == HttpStatus.OK.value()
        resp.body().string() == "hello"
    }

    void "testing post"() {
        when:
        Response resp = post("$path/smoke?foo=bar", [num: "123", name: "up"])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.num
        body.foo == "bar"
        body.name == 'up'

    }

}
