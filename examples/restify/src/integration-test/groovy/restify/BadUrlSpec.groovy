package restify

import groovy.json.JsonSlurper

import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification

@Integration
class BadUrlSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/this/is/no/good"

    //@IgnoreRest
    void "good url"() {
        when:
        Response resp = get('/api/appConfig/rally/org')
        String bodyString = resp.body().string()
        println "bodyString: ${bodyString}"

        then:
        resp.code() == HttpStatus.OK.value()
        Map body = new JsonSlurper().parseText(bodyString) as Map

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
