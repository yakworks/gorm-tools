package yakworks.gorm.api

import org.springframework.beans.factory.annotation.Autowired

import yakworks.gorm.api.ApiConfig
import spock.lang.Specification
import yakworks.gorm.api.ApiUtils
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class ApiUtilsSpec extends Specification implements GrailsAppUnitTest {
    static springBeans = [apiConfig: ApiConfig]

    @Autowired ApiConfig apiConfig

    def "splitPath"() {
        expect:
        ApiUtils.splitPath("/foo/bar") == [name: 'bar', namespace: 'foo']
        ApiUtils.splitPath("foo/bar") == [name: 'bar', namespace: 'foo']
        ApiUtils.splitPath("/bar") == [name: 'bar', namespace: '']
    }

    def "parse query params"() {
        expect:
        ApiUtils.parseQueryParams("") == [:]
        ApiUtils.parseQueryParams(null) == [:]

        ApiUtils.parseQueryParams("name=abc.zip") == [name:"abc.zip"]
        ApiUtils.parseQueryParams("na%20me=ab%20c.zip") == ["na me":"ab c.zip"]
        ApiUtils.parseQueryParams("name=abc.zip&size=111") == [name:"abc.zip", size:"111"]
        ApiUtils.parseQueryParams("name=") == [name:""]
        ApiUtils.parseQueryParams("name=&size=10") == [name:"", size:"10"]
        ApiUtils.parseQueryParams("name=test.zip&size=") == [name:"test.zip", size:""]
        ApiUtils.parseQueryParams("name=test.zip&size") == [name:"test.zip", size:""]
    }
}
