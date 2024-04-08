/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.extensions


import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import spock.lang.Specification
import yakworks.rally.common.ZonedDateUtil

class AppTimeZoneSpec extends Specification {

    void "test now"() {
        when:
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        var etz = TimeZone.getTimeZone("America/New_York")
        AppTimeZone.setTimeZone(etz)

        var ldNow = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        var ldEt = AppTimeZone.localDateTimeNow().truncatedTo(ChronoUnit.MINUTES)
        int hrsOffset = ZonedDateUtil.hoursOffset(etz.toZoneId())

        then:
        //offset should be either 4 or 5, depends on whether its daylight savings.
        assert hrsOffset in [-4,-5]
        ChronoUnit.HOURS.between(ldNow, ldEt) == hrsOffset
        //ldEt.toString() == ldNow.toString()
    }

    def "test nowAppZone"() {
        expect:
        //truncate to minutes as they will be slightly off
        //NOTE: its possible
        LocalDate.nowAppZone() == LocalDate.now(ZoneId.appDefault())
        checkSameDate(LocalDateTime.nowAppZone(), LocalDateTime.now(ZoneId.appDefault()))
        checkSameDate(ZonedDateTime.nowAppZone(), ZonedDateTime.now(ZoneId.appDefault()))
    }

    boolean checkSameDate(LocalDateTime t1, LocalDateTime t2){
        return t1.truncatedTo(ChronoUnit.MINUTES) == t2.truncatedTo(ChronoUnit.MINUTES)
    }

    boolean checkSameDate(ZonedDateTime t1, ZonedDateTime t2){
        return t1.truncatedTo(ChronoUnit.MINUTES) == t2.truncatedTo(ChronoUnit.MINUTES)
    }
}
