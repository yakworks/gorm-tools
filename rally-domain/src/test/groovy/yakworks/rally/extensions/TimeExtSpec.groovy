package yakworks.rally.extensions

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.config.AppRallyConfig
import yakworks.testing.grails.GrailsAppUnitTest

class TimeExtSpec extends Specification implements GrailsAppUnitTest  {
    @Autowired AppRallyConfig rallyConfig

    Closure doWithSpring() { { ->
        rallyConfig(AppRallyConfig)
    }}

    def "test nowAppZone"() {
        expect:
        LocalDate.nowAppZone() == LocalDate.now(ZoneId.appDefault())

    }

    def "test nowAppZone DateTime"() {
        expect:
        LocalDate.nowAppZone() == LocalDate.now(ZoneId.appDefault())
        checkSameDate(LocalDateTime.nowAppZone(), LocalDateTime.now(ZoneId.appDefault()))
        checkSameDate(ZonedDateTime.nowAppZone(), ZonedDateTime.now(ZoneId.appDefault()))
    }

    //truncate to minutes as they will be slightly off
    //NOTE: its possible that miliseconds could move up to next minute. chances are very small but could happen and fail test here.
    boolean checkSameDate(LocalDateTime t1, LocalDateTime t2){
        return t1.truncatedTo(ChronoUnit.MINUTES) == t2.truncatedTo(ChronoUnit.MINUTES)
    }

    boolean checkSameDate(ZonedDateTime t1, ZonedDateTime t2){
        return t1.truncatedTo(ChronoUnit.MINUTES) == t2.truncatedTo(ChronoUnit.MINUTES)
    }

}
