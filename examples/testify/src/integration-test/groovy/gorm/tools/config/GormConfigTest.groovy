package gorm.tools.config


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.gorm.config.GormConfig
import yakworks.testing.gorm.integration.DataIntegrationTest

/**
 * sanity checking that @ConfigurationProperties works
 */
@Integration
@Rollback
class GormConfigTest extends Specification implements DataIntegrationTest {

    @Autowired GormConfig gormConfig

    def "sanity Check"() {
        expect:
        //gormConfig.hello == "world"
        gormConfig.idGenerator.startValue == 999
    }

}
