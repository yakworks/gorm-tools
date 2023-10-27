package yakworks.rally.extensions

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

import groovy.transform.CompileStatic

@CompileStatic
class TimeStaticExt {

    /**
     * Uses the app default zone to get the current date.
     * The system zone should be set to UTC. The App can have a default time zone.
     *
     * So for example when I want today and its 9:00pm Eastern, its tomorrow at 1am in UTC so using the default
     * LocalDate.now() give a date for tomorrow.
     *
     * @param type
     * @return the LocalDate in the default time zone.
     */
    static LocalDate nowAppZone(final LocalDate type) {
        LocalDate.now(AppTimeZone.zoneId)
    }

    static LocalDateTime nowAppZone(final LocalDateTime type) {
        LocalDateTime.now(AppTimeZone.zoneId)
    }

    /**
     * Sister to ZoneId.systemDefault()
     * Gets the App default time-zone.
     * The system zone will normally be set to UTC.
     * The App can have a different default time zone for where most of the users are.
     *
     * So for example when I want today and its 9:00pm Eastern, its tomorrow at 1am in UTC so using the default
     * LocalDate.now() give a date for tomorrow.
     *
     * @param type
     * @return the LocalDate in the default time zone.
     */
    static ZoneId appDefault(final ZoneId type) {
        return AppTimeZone.zoneId
    }
}
