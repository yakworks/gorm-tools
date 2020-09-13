package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiTestTrait
import grails.testing.mixin.integration.Integration

@Integration
class LocationRestApiSpec extends GebSpec implements RestApiTestTrait {
    String path = "api/location"
    Map postData = [city: "foo"]
    Map putData = [city: "city2"]

    void "exercise api"() {
        expect:
        testGet()
        testPost()
        testPut()
        testDelete()
    }
}
