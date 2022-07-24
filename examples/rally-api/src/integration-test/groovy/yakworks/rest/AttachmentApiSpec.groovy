package yakworks.rest

import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Response
import spock.lang.Specification

@Integration
class AttachmentApiSpec extends Specification implements OkHttpRestTrait {

    void "sanity check create create"() {
        when:
        Response resp = post("/api/rally/attachment", [name: "flub.txt"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.name == 'flub.txt'
    }

    void "check to make sure action urlmapping works"() {
        when:
        Response resp = post("/api/rally/attachment/upload", [num: "foobie123", name: "flub"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.name == 'flub'
    }

}
