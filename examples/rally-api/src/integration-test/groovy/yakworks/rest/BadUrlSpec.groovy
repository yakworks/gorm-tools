package yakworks.rest

import groovy.json.JsonSlurper

import org.springframework.http.HttpStatus

import yakworks.rally.api.SpringApplication
import yakworks.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification

@Integration(applicationClass = SpringApplication)
class BadUrlSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/this/is/no/good"

    def setup(){
        login()
    }

    void "good url"() {
        when:
        Response resp = get('/api/rally/org?q=*')
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
        body.status == 404
        // body.error == 404
    }

}
