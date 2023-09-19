package yakworks.rally.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.TestPropertySource

import spock.lang.Specification
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class RallyConfigSpec extends Specification implements GrailsAppUnitTest {

    @Value('${app.resources-dir}')
    String rootDir

    @Value('${app.resources.rootLocation}')
    String appResourcesDir

    @Autowired RallyConfig rallyConfig
    @Autowired OrgConfig orgConfig

    Closure doWithSpring() { { ->
        rallyConfig(RallyConfig)
        orgConfig(OrgConfig)
    }}

    def "sanity Check"() {
        expect:
        rootDir == "./build/rootLocation"
        appResourcesDir == rootDir
        rallyConfig.resourcesDir == "./build/rootLocation"
        rallyConfig.hello == "world"
    }

    def "orgs Check"() {
        expect:
        orgConfig.partitionOrgType == OrgType.Division
        orgConfig.dimensions[0] == 'CustAccount.Customer.Division.Business'

    }

}
