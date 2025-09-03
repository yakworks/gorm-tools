package yakworks.security

import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

@Integration
class ApiPermissionsSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/org"

    void setupSpec() {
        OkAuth.TOKEN = null
    }

    void cleanupSpec() {
        OkAuth.TOKEN = null
    }

    void "unauthorized when user is not logged in"() {
        when: "GET"
        Response resp = get("$path/1")

        then:
        resp.code() == HttpStatus.UNAUTHORIZED.value()
    }

    void "forbidden when user is logged in but does not have permission"() {
        setup:
        login("noroles", "123")
        when: "GET"
        Response resp = get("$path/1")

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "POST"
        resp = post(path, [:])

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "PUT"
        resp = put(path + "/1", [:])

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "DELETE"
        resp = delete(path, 1)

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        cleanup:
        OkAuth.TOKEN = null
    }

    void "logged in and has permission"() {
        setup: "login as admin, it has all the permissions"
        login("admin", "123")

        when: "GET"
        Response resp = get("$path/1")

        then:
        resp.code() == HttpStatus.OK.value()

        when: "POST"
        resp = post(path, [num:'ptest', name:'ptest', type: "Customer"])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id

        when: "PUT"
        resp = put(path + "/${body.id}", [name:"ptest2"])
        body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.name == "ptest2"

        when: "DELETE"
        resp = delete(path, body.id)

        then:
        resp.code() == HttpStatus.NO_CONTENT.value()

        cleanup:
        OkAuth.TOKEN = null
    }
}
