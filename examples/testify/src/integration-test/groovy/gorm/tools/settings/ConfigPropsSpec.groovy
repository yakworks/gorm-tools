package gorm.tools.settings


import gorm.tools.config.AsyncConfig
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.testing.gorm.integration.DataIntegrationTest

/**
 * sanity checking that @ConfigurationProperties works
 */
@Integration
@Rollback
class ConfigPropsSpec extends Specification implements DataIntegrationTest {

    @Autowired
    AsyncConfig asyncConfig

    def "sanity Check"() {
        expect:
        asyncConfig.poolSize == 2
    }

}
