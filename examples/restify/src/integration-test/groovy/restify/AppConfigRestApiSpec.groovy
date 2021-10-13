package restify

import org.springframework.http.HttpStatus

import gorm.tools.json.JsonParserTrait
import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification

// import grails.gorm.transactions.Rollback

@Integration
class AppConfigRestApiSpec extends Specification implements OkHttpRestTrait, JsonParserTrait {

    void "test config values"() {
        when:
        Response resp = get('/api/appConfig/rally/org')
        println "bodyString: ${resp.body().string()}"
        Map body = bodyToMap(resp) as Map

        then: "should have excluded the flattened spring array keys"
        resp.code() == HttpStatus.OK.value()
        body.includes.get == ['*', 'info.*', "location.id"]
    }

}
