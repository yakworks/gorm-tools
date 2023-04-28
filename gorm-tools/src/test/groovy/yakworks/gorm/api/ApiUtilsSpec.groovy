package yakworks.gorm.api

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
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

        when:
        Map params = ApiUtils.parseQueryParams("includes=custNum,refnum,amount,%20origAmount,%20tranDate,%20tranType.name,ponum&max=1000000000&format=csv&q={%22state%22:[0],%22member%22:{%22division%22:{%22num%22:%22CO%22}}")

        then:
        noExceptionThrown()
        params.size() == 4
        params.keySet().containsAll(["includes", "max", "format", "q"])
        params.includes == "custNum,refnum,amount, origAmount, tranDate, tranType.name,ponum"
        params.max == "1000000000"
        params.q == '{"state":[0],"member":{"division":{"num":"CO"}}'
    }
}
