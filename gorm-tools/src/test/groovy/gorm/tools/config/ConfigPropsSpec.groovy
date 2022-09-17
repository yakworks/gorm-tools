package gorm.tools.config


import gorm.tools.settings.AsyncProperties
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.testing.gorm.integration.DataIntegrationTest
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class ConfigPropsSpec extends Specification implements GrailsAppUnitTest {

    @Autowired
    AsyncProperties asyncProperties

    def "sanity Check"() {
        expect:
        asyncProperties.poolSize == 2
    }

}
