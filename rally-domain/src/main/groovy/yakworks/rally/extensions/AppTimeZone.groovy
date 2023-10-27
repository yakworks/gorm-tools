/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.extensions

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

import groovy.transform.CompileStatic

/**
 * Holder for the App default time-zone.
 * The system zone will normally be set to UTC.
 * The App can have a different default time zone for where most of the users are
 * or the timezone accounting is based on. A GL posting date for example is usually anchored to a timezone for balancing purposes.
 *
 * So for example when I want an invoice or GL date for today and its 9:00pm Eastern time (new york)
 * then its tomorrow at 1am in UTC so using the vanilla java LocalDate.now() will give a date for tomorrow when the accounting books
 * are still open and running for today.
 */
@CompileStatic
class AppTimeZone {

    static TimeZone APP_ZONE = TimeZone.getTimeZone("America/New_York")

    static void setTimeZone(TimeZone zone){
        APP_ZONE = zone
    }

    static TimeZone getTimeZone(){
        APP_ZONE
    }

    static ZoneId getZoneId(){
        APP_ZONE.toZoneId()
    }
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
    static LocalDate localDateNow() {
        assert APP_ZONE
        LocalDate.now(getZoneId())
    }

    static LocalDateTime localDateTimeNow() {
        assert APP_ZONE
        LocalDateTime.now(getZoneId())
    }

}
