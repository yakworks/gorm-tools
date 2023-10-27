package yakworks.rally.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import spock.lang.Specification
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
    @Autowired OrgProps orgProps

    Closure doWithSpring() { { ->
        rallyConfig(RallyConfig)
        orgProps(OrgProps)
    }}

    def "sanity Check"() {
        expect:
        rootDir == "./build/rootLocation"
        appResourcesDir == rootDir
        rallyConfig.resourcesDir == "./build/rootLocation"
        rallyConfig.hello == "world"
        rallyConfig.defaults.currency.currencyCode == 'USD'
        rallyConfig.defaults.timeZone.toZoneId().toString() == "America/New_York"
    }

    def "orgs Check"() {
        expect:
        orgProps.partition.enabled
        orgProps.partition.type == OrgType.Division
        //orgProps.members.dimension == [OrgType.CustAccount, OrgType.Customer, OrgType.Division, OrgType.Business]
    }

}
