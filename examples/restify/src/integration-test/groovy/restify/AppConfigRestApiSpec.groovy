package restify

import org.springframework.test.annotation.Rollback

import geb.spock.GebSpec
import gorm.tools.rest.client.RestApiTestTrait
import grails.testing.mixin.integration.Integration

// import grails.gorm.transactions.Rollback

import static org.springframework.http.HttpStatus.OK

@Integration
@Rollback
class AppConfigRestApiSpec extends GebSpec implements RestApiTestTrait {

    String path = "api/appConfig"

    void "test config values"() {
        when:
        def response = restBuilder.get("${baseUrl}api/appConfig/project")

        then:
        response.json.includes.list == ['id', 'num', 'name', 'billable']
        response.json.includes.picklist == ['id', 'num', 'name']
        response.json.includes."list[0]" == null
        response.json.includes."picklist[0]" == null
    }



}
