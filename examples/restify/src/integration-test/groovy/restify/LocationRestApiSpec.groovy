package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiFuncSpec
import gorm.tools.rest.testing.RestApiTestTrait
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import restify.domain.Location

import static grails.web.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.*

@Integration
@Rollback
//Copied all stuff here, because we cant override test cases, but need to test controller method Overriding
class LocationRestApiSpec extends RestApiFuncSpec {

    Class<Location> domainClass = Location
    boolean vndHeaderOnError = false

    String getResourcePath() {
        "${baseUrl}api/location"
    }

    //data to force a post or patch failure
    Map getInvalidData() { [city: null] }

    Map postData = [city: "foo"]

    Map putData = [city: "city"]

}
