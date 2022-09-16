package gorm.tools.settings

import java.time.LocalDate

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex
import yakworks.testing.gorm.integration.DataIntegrationTest

/**
 * sanity checking that @ConfigurationProperties works
 */
@Integration
@Rollback
class ConfigPropsSpec extends Specification implements DataIntegrationTest {

    @Autowired
    AsyncProperties asyncProperties

    def "sanity Check"() {
        expect:
        asyncProperties.poolSize == 2
    }

}
