/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import org.springframework.util.StringUtils

import spock.lang.Specification

class ZonedDateUtilSpec extends Specification {

    void "toLocalDateTimeZone"() {
        when:
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        ZonedDateTime zdtUTCNow = ZonedDateTime.now()
        var eastZone = ZoneId.of("America/New_York")
        LocalDateTime ldEt = ZonedDateUtil.toLocalDateTimeZone(zdtUTCNow, eastZone)

        int hrsOffset = ZonedDateUtil.hoursOffset(eastZone)

        then:
        //offset should be either 4 or 5, depends on whether its daylight savings.
        assert hrsOffset in [-4,-5]
        ZonedDateUtil.hoursBetween(zdtUTCNow.toLocalDateTime(), ldEt) == hrsOffset
        //ldEt.toString() == ldNow.toString()
    }

    void "toZonedDateTime"() {
        when:
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        ZonedDateTime zdtUTCNow = ZonedDateTime.now()
        var eastZone = ZoneId.of("America/New_York")
        ZonedDateTime zdEt = ZonedDateUtil.toZonedDateTime(zdtUTCNow, eastZone)

        int hrsOffset = ZonedDateUtil.hoursOffset(eastZone)

        then:
        //offset should be either 4 or 5, depends on whether its daylight savings.
        assert hrsOffset in [-4,-5]
        ChronoUnit.HOURS.between(zdtUTCNow.toLocalDateTime(), zdEt.toLocalDateTime()) == hrsOffset
        //ldEt.toString() == ldNow.toString()
    }

    void "isSameMinute isSameSecond"() {
        when:
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        ZonedDateTime zdtUTCNow = ZonedDateTime.now()
        var eastZone = ZoneId.of("America/New_York")
        ZonedDateTime zdEt = ZonedDateUtil.toZonedDateTime(zdtUTCNow, eastZone)

        then:
        ChronoUnit.MINUTES.between(zdtUTCNow, zdEt) == 0
        //ChronoUnit.MINUTES.between(zdtUTCNow.toLocalDateTime(), zdEt.toLocalDateTime()) == 0
        ZonedDateUtil.isSameMinute(zdtUTCNow, zdEt)
        ZonedDateUtil.isSameSecond(zdtUTCNow, zdEt)
        //but wont be equal as milliseconds are off
        zdtUTCNow == zdEt
    }

    void "isSameMinute isSameSecond same"() {
        when:
        ZonedDateTime zdtTime1 = ZonedDateTime.now(ZoneId.of("America/New_York"))
        ZonedDateTime zdtTime2 = ZonedDateTime.now(ZoneId.of("America/New_York")) // ZonedDateUtil.toZonedDateTime(zdtTime1, eastZone)

        then:
        ZonedDateUtil.isSameMinute(zdtTime1, zdtTime2)
        ZonedDateUtil.isSameSecond(zdtTime1, zdtTime2)
        //but wont be equal as milliseconds are off
        zdtTime1 != zdtTime2
        //unlikely this will fail but its possible if zdtTime2 is the next minute.
        zdtTime1.truncatedTo(ChronoUnit.MINUTES) == zdtTime2.truncatedTo(ChronoUnit.MINUTES)
    }

    void "test timezone playground"() {
        setup:
        //var tz = TimeZone.getDefault()
        var tz1 = StringUtils.parseTimeZoneString("CST")
        var utc = StringUtils.parseTimeZoneString("UTC")

        expect:
        StringUtils.parseTimeZoneString("CST").toZoneId().toString() == "America/Chicago"
        StringUtils.parseTimeZoneString("PST").toZoneId().toString() == "America/Los_Angeles"
        // StringUtils.parseTimeZoneString("MST").toZoneId().toString() == "America/Denver"
        // StringUtils.parseTimeZoneString("EST").toZoneId().toString() == "America/New_York"
        utc.toZoneId() == ZoneId.of("UTC")
        //tz.toZoneId() == ZoneId.of("America/Denver")
        tz1.toZoneId().toString() == "America/Chicago"
        LocalDateTime localDateTime = LocalDateTime.parse("2023-09-20T13:59:59")
        localDateTime.toString() == "2023-09-20T13:59:59"
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("America/Chicago"))
        zonedDateTime.toString() == "2023-09-20T13:59:59-05:00[America/Chicago]"
        //central time
        var zonedDateTime2 = LocalDateTime.parse("2023-11-06T13:59:59").atZone(ZoneId.of("America/Chicago"))
        zonedDateTime2.toString() == "2023-11-06T13:59:59-06:00[America/Chicago]"
        zonedDateTime2.withZoneSameInstant(ZoneId.of("UTC")).toString() == "2023-11-06T19:59:59Z[UTC]"
        zonedDateTime2.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime().toString() == "2023-11-06T19:59:59"
    }

    void "local date zone conversion playground"() {
        setup:
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        expect:
        ZoneId.of("America/New_York").toString() == "America/New_York"
        var ldt = LocalDateTime.parse("2023-09-20T01:01:01")
        ldt.toString() == "2023-09-20T01:01:01"
        ldt.toDate().toString() == "Wed Sep 20 01:01:01 UTC 2023"
        // LocalDateTime.ofInstant(ldt.toInstant(ZoneOffset.of()))
        var utc_ldt = ldt.atZone(ZoneId.of("UTC"))
        utc_ldt.toString() == "2023-09-20T01:01:01Z[UTC]"
        ZonedDateTime et_ldt = utc_ldt.withZoneSameInstant(ZoneId.of("America/New_York"))
        et_ldt.toString() == "2023-09-19T21:01:01-04:00[America/New_York]"
        et_ldt.toLocalDateTime().toString() == "2023-09-19T21:01:01"
        new SimpleDateFormat("yyMMdd").format(et_ldt.toLocalDateTime().toDate()) == "230919"
        et_ldt.toLocalDateTime().toDate().toString() == "Tue Sep 19 21:01:01 UTC 2023"
        //without changing to LocalDateTime first it keeps timezone and adjusts it
        et_ldt.toDate().toString() == "Wed Sep 20 01:01:01 UTC 2023"

        var ldNow = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        var ldEt = LocalDateTime.now(ZoneId.of("America/New_York")).truncatedTo(ChronoUnit.MINUTES)
        ChronoUnit.HOURS.between(ldEt, ldNow) == 4
        //ldEt.toString() == ldNow.toString()
    }

}
