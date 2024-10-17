package yakworks.gorm.api


import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class ApiConfigSpec extends Specification implements GrailsAppUnitTest {

    static springBeans = [ApiConfig]

    @Autowired ApiConfig apiConfig


    def "sanity Check"() {
        expect:
        apiConfig.namespaces
        apiConfig.paths.size() == 2
        apiConfig.paths['/testing/sinkExt'].includes['getCustom'] ==['id', 'name', 'thing.$stamp' ]
        apiConfig.paths['/testing/sinkExt'] == apiConfig.pathsByEntity['yakworks.testing.gorm.model.SinkExt']
    }

}
