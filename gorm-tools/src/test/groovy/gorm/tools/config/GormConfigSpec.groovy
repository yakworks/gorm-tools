package gorm.tools.config

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class GormConfigSpec extends Specification implements GrailsAppUnitTest {

    @Autowired AsyncConfig asyncConfig
    @Autowired GormConfig gormConfig
    @Autowired IdGeneratorConfig idGeneratorConfig
    @Autowired ApiProperties apiProperties

    def "sanity Check"() {
        expect:
        apiProperties.namespaces
        apiProperties.paths.size() == 1
        apiProperties.paths.testing.size() == 2
        apiProperties.paths['testing'].sinkExt.includes['getCustom'] ==['id', 'name', 'thing.$stamp' ]

        asyncConfig.poolSize == 2
        asyncConfig.sliceSize == 50
        gormConfig.async.poolSize == 2
        gormConfig.async.sliceSize == 50

        gormConfig.hello == "world"

        idGeneratorConfig.startValue == 1
        gormConfig.idGenerator.startValue == 1
    }

}
