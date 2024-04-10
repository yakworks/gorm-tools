/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

import groovy.transform.CompileStatic

@CompileStatic
class ZonedDateUtil {

    static LocalDateTime toLocalDateTimeZone(ZonedDateTime zonedDateTime, ZoneId toZoneId){
        return zonedDateTime
            .withZoneSameInstant(toZoneId)
            .toLocalDateTime()
    }

    static ZonedDateTime toZonedDateTime(ZonedDateTime zonedDateTime, ZoneId toZoneId){
        return zonedDateTime.withZoneSameInstant(toZoneId)
    }

    static LocalDateTime toLocalDateTimeZone(LocalDateTime localDateTime, ZoneId fromZoneId, ZoneId toZoneId){
        return localDateTime.atZone(fromZoneId)
            .withZoneSameInstant(toZoneId)
            .toLocalDateTime()
    }

    //returns hours offset from UTC
    static int hoursOffset(ZoneId zoneId){
        var offset = zoneId.getOffset()
        BigDecimal hrs = offset.totalSeconds / 60 / 60
        return hrs.toInteger()
    }

    //for testing, returns hours between 2 times.
    static long hoursBetween(LocalDateTime ldtInclusive, LocalDateTime ldtExclusive) {
        return ChronoUnit.HOURS.between(ldtInclusive, ldtExclusive)
    }

    /** used for testing to see if 2 times are within 60 seconds of each other */
    static boolean isSameMinute(Temporal t1, Temporal t2){
        ChronoUnit.SECONDS.between(t1, t2).abs() < 60
    }

    /** used for testing to see if 2 times are within 60 seconds of each other */
    static boolean isSameSecond(Temporal t1, Temporal t2){
        ChronoUnit.SECONDS.between(t1, t2).abs() < 1
    }
}
