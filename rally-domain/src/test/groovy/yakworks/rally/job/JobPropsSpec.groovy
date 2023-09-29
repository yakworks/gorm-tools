package yakworks.rally.job

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.config.JobProps
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class JobPropsSpec extends Specification implements GrailsAppUnitTest {

    @Autowired JobProps jobProps

    Closure doWithSpring() { { ->
        jobProps(JobProps)
    }}

    def "sanity Check"() {
        expect:
        jobProps.maintenanceWindow.size() == 2
    }

}
