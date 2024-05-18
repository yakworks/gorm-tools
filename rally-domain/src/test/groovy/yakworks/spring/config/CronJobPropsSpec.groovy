package yakworks.spring.config


import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class CronJobPropsSpec extends Specification implements GrailsAppUnitTest {

    @Autowired CronJobProps cronJobProps

    Closure doWithSpring() { { ->
        cronJobProps(CronJobPropsImpl)
    }}

    def "sanity Check"() {
        expect:
        cronJobProps.cron == '* * 2,3 * * TUE-SAT'
        cronJobProps.timeZone == TimeZone.getTimeZone("America/New_York")
    }

}
