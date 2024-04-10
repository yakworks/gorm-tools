package yakworks.rally.job

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.config.MaintenanceProps
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class MaintenancePropsSpec extends Specification implements GrailsAppUnitTest {

    @Autowired MaintenanceProps maintenanceProps

    Closure doWithSpring() { { ->
        maintenanceProps(MaintenanceProps)
    }}

    def "sanity Check"() {
        //checks against whats in the MaintenanceProps in application.yml
        expect:
        maintenanceProps.crons.size() == 2
        maintenanceProps.timeZone == TimeZone.getTimeZone("CST")
    }

}
