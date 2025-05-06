package yakworks.rest

import org.springframework.http.HttpStatus


import yakworks.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import yakworks.json.groovy.JsonEngine

// import grails.gorm.transactions.Rollback

@Integration
class AppConfigRestApiSpec extends Specification implements OkHttpRestTrait {

    def setup(){
        login()
    }

    void "test config values"() {
        when:
        Response resp = get('/api/appConfig/rally/org')
        String bodyString = resp.body().string()
        println "bodyString: ${bodyString}"
        Map body = JsonEngine.parseJson(bodyString, Map)

        then: "should have excluded the flattened spring array keys"
        resp.code() == HttpStatus.OK.value()
        body.includes.get == [ '*', 'calc.totalDue', 'contact.$*', 'tags', 'contact.flex.num1'  ]
    }

}
