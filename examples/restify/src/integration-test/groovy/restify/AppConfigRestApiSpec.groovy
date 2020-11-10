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
        def response = restBuilder.get("${baseUrl}api/appConfig/org")

        then:
        response.json.includes.get == ['*', 'type.*', 'status.*']
        response.json.includes.picklist == ['id', 'name']
        response.json.includes."get[0]" == null
        response.json.includes."picklist[0]" == null
        response.json.form[0].selectOptions.dataApiParams.or != null
        response.json.form[0].selectOptions.dataApiParams."or[0]" == null
    }



}
