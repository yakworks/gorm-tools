package restify

import groovy.json.JsonSlurper

import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification

// import grails.gorm.transactions.Rollback

@Integration
class AppConfigRestApiSpec extends Specification implements OkHttpRestTrait {

    void "test config values"() {
        when:
        Response resp = get('/api/appConfig/rally/org')
        String bodyString = resp.body().string()
        println "bodyString: ${bodyString}"
        Map body = new JsonSlurper().parseText(bodyString) as Map

        then: "should have excluded the flattened spring array keys"
        resp.code() == HttpStatus.OK.value()
        body.includes.get == ['*', 'info.*']
    }

}
