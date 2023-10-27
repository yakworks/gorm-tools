package yakworks.rally.extensions

import java.time.LocalDate
import java.time.ZoneId

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.config.RallyConfig
import yakworks.testing.grails.GrailsAppUnitTest

class TimeExtSpec extends Specification implements GrailsAppUnitTest  {
    @Autowired RallyConfig rallyConfig

    Closure doWithSpring() { { ->
        rallyConfig(RallyConfig)
    }}

    def "test nowAppZone"() {
        expect:
        LocalDate.nowAppZone() == LocalDate.now(ZoneId.appDefault())

    }

}
