package gorm.tools.config


import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class GormConfigSpec extends Specification implements GrailsAppUnitTest {

    @Autowired AsyncSettings asyncConfig
    @Autowired GormConfig gormConfig

    def "sanity Check"() {
        expect:
        asyncConfig.poolSize == 2
        gormConfig.hello == "world"
        gormConfig.idGenerator.startValue == 1
    }

}
