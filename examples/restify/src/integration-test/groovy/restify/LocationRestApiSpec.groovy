package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import grails.testing.mixin.integration.Integration

@Integration
class LocationRestApiSpec extends RestApiFuncSpec {
    String path = "api/location"
    Map postData = [city: "foo"]
    Map putData = [city: "city2"]
}
