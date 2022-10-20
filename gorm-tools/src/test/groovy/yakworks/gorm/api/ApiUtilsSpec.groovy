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

}
