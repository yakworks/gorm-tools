/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job


import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

import org.springframework.util.StringUtils

import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem

class MaintWindowUtilSpec extends Specification {

    void "test timezone playground"() {
        setup:
        var tz = TimeZone.getDefault()
        var tz1 = StringUtils.parseTimeZoneString("CST")
        var utc = StringUtils.parseTimeZoneString("UTC")

        expect:
        utc.toZoneId() == ZoneId.of("UTC")
        ZoneId.of("America/Denver")
        tz.toZoneId() == ZoneId.of("America/Denver")
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

    void "test secondsToNextRun"() {
        setup:
        String cron = "* * 14,15 * * MON-FRI"

        expect:
        1 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T13:59:59"))
        60 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T13:59:00"))
        //in middle
        1 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T15:00"))
        //at end
        79200 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T16:00"))

    }

    void "test checkWindows catch all"() {
        when:
        //catch all for all hours and all days, will ALWAYS throw error with cron like this
        List<String> cronList = ['* * 0-23 * * MON-SUN']
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.now())
        then:
        thrown(ThrowableProblem)
    }

    void "test checkWindows"() {
        when:
        // # 9pm-11pm MON-FRI Central time, which in UTC is 02:00-04:00 TUE-SAT
        // # set the hours with comma instead of range, so we say all of hour 02 and all of hour 03, which wont include hour 04.
        // - '* * 2,3 * * TUE-SAT'
        // # 3pm-7pm SAT-SUN Central time, which in UTC is 20:00-24:00 SAT,SUN
        // - '* * 20,21,22,23 * * SAT,SUN'

        List<String> cronList = ['* * 2,3 * * TUE-SAT', '* * 20,21,22,23 * * SAT,SUN']

        // NOTE: 2023-09-18 is a MON. 2023-09-24 is SUN
        then:
        //1 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T13:59:59"))
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T01:59:00"))
        // at end
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T04:00"))

        when: "at start m-f within a second"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T01:59:59"))

        then:
        var e = thrown(ThrowableProblem)
        e.status.code == 503

        when: "im middle m-f"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T03:00:00"))

        then:
        e = thrown(ThrowableProblem)
        e.status.code == 503

        when: "at end m-f"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T04:00:00"))

        then:
        noExceptionThrown()

        when: "SUN at M-F time"
        //make sure the first one doesnt pick up and only the weekend one picks up
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-24T03:00:00"))

        then:
        noExceptionThrown()

        when: "Sat at 3-7 start time"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-23T20:00:00"))

        then:
        e = thrown(ThrowableProblem)
        e.status.code == 503

    }

    void "test checkWindows central time"() {
        when:
        // # 9pm-11pm MON-FRI Central time, which in UTC is 02:00-04:00 TUE-SAT
        // # set the hours with comma instead of range, so we say all of hour 02 and all of hour 03, which wont include hour 04.
        // - '* * 2,3 * * TUE-SAT'
        // # 3pm-7pm SAT-SUN Central time, which in UTC is 20:00-24:00 SAT,SUN
        // - '* * 20,21,22,23 * * SAT,SUN'

        List<String> cronList = ['* * 2,3 * * TUE-SAT', '* * 20,21,22,23 * * SAT,SUN']

        // NOTE: 2023-09-18 is a MON. 2023-09-24 is SUN
        then:
        //8:59 pm MONDAY
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-18T20:59:00"))
        //11:00pm MON
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-18T23:00"))

        when: "8:59 pm MON within a second"
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-18T20:59:59"))

        then:
        var e = thrown(ThrowableProblem)
        e.status.code == 503

        when: "im middle at 10pm on WED"
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-20T22:00:00"))

        then:
        e = thrown(ThrowableProblem)
        e.status.code == 503

        when: "at 11pm Wed"
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-20T23:00:00"))

        then:
        noExceptionThrown()

        when: "Weekend"
        //make sure the first one doesnt pick up and only the weekend one picks up
        //SAT at 9pm
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-23T21:00:00"))
        //SUN at 9pm
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-24T21:00:00"))

        then:
        noExceptionThrown()

        when: "Sat at 3pm start time"
        MaintWindowUtil.checkWindows(cronList, centralToUTC("2023-09-23T15:00:00"))

        then:
        e = thrown(ThrowableProblem)
        e.status.code == 503

    }

    LocalDateTime centralToUTC(String sdate){
        LocalDateTime localDateTime = LocalDateTime.parse(sdate)
        return localDateTime.atZone(ZoneId.of("America/Chicago"))
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toLocalDateTime()
    }
}
