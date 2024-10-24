package yakworks.gorm.config

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.gorm.config.AsyncConfig
import yakworks.gorm.config.GormConfig
import yakworks.gorm.config.IdGeneratorConfig
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class GormConfigSpec extends Specification implements GrailsAppUnitTest {

    @Autowired AsyncConfig asyncConfig
    @Autowired GormConfig gormConfig
    @Autowired IdGeneratorConfig idGeneratorConfig

    def "sanity Check"() {
        expect:
        asyncConfig.poolSize == 2
        asyncConfig.sliceSize == 50
        gormConfig.async.poolSize == 2
        gormConfig.async.sliceSize == 50

        idGeneratorConfig.startValue == 1
        gormConfig.idGenerator.startValue == 1
    }

}
