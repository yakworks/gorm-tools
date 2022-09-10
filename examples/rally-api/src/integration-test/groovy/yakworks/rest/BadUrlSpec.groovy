package yakworks.rest

import groovy.json.JsonSlurper

import org.springframework.http.HttpStatus

import yakworks.gorm.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification

@Integration
class BadUrlSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/this/is/no/good"

    void "good url"() {
        when:
        Response resp = get('/api/rally/org')
        String bodyString = resp.body().string()

        then:
        resp.code() == HttpStatus.OK.value()
    }

    void "bad url"() {
        when:
        Response resp = get("/api/this/is/no/good")
        String bodyString = resp.body().string()
        println "bodyString: ${bodyString}"

        then:
        resp.code() == HttpStatus.NOT_FOUND.value()
        Map body = new JsonSlurper().parseText(bodyString) as Map
        body.error == 404
    }

}
