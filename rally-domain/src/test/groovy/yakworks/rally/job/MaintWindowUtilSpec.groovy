/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem
import yakworks.commons.lang.ZonedDateUtil
import yakworks.rally.config.MaintenanceProps

class MaintWindowUtilSpec extends Specification {

    void "test secondsToNextRun"() {
        setup:
        String cron = "* * 14,15 * * MON-FRI"

        expect:
        //within a second
        1 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T13:59:59"))
        //60 seconds away
        60 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T13:59:00"))
        //in the middle
        1 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T15:00"))
        //at end
        79200 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T16:00"))

    }

    void "check with null everything is always true"() {
        //means that no maint schedule is setup, so never a blackout
        when:
        MaintWindowUtil.check(new MaintenanceProps())
        MaintWindowUtil.check([], null)
        MaintWindowUtil.check(null, null)

        then: 'should always work'
        noExceptionThrown()

    }

    void "test checkWindows catch all"() {
        when:
        //catch all for all hours and all days, will ALWAYS throw error with cron like this
        List<String> cronList = ['* * 0-23 * * MON-SUN']
        MaintWindowUtil.check(cronList)

        then:
        thrown(ThrowableProblem)

        when:
        MaintWindowUtil.check(cronList, TimeZone.getTimeZone("America/New_York").toZoneId())

        then:
        thrown(ThrowableProblem)

        when:
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.now())

        then:
        thrown(ThrowableProblem)
    }

    void "test currentTime"() {
        when:
        var nyTZ = TimeZone.getTimeZone("America/New_York")
        LocalDateTime curTime = MaintWindowUtil.currentTime(nyTZ.toZoneId())
        LocalDateTime curTimeNoZone = MaintWindowUtil.currentTime(null)

        then:
        ZonedDateUtil.isSameSecond(curTime, LocalDateTime.now(nyTZ.toZoneId()))
        ZonedDateUtil.isSameSecond(curTimeNoZone, LocalDateTime.now())
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

    void "test checkWindows MaintProps"() {
        when:
        var tz = TimeZone.getTimeZone("America/New_York")
        var nyTime = ZonedDateTime.now(tz.toZoneId())
        //to prevent test from failing because it rolls to next hour, sleep until it does, should not take more than 2 sceonds to run
        if(nyTime.getMinute() == 59 && nyTime.getSecond() > 57 ){
            sleep 2000
            //redo it so we have same hour as we will have when MaintWindowUtil gets current time
            nyTime = ZonedDateTime.now(tz.toZoneId())
        }

        List<String> cronList = ["* * ${nyTime.getHour()} * * MON-SUN".toString()]
        MaintenanceProps mProps = new MaintenanceProps(
            crons: cronList,
            timeZone: tz
        )

        //should fail with current time
        MaintWindowUtil.check(mProps)

        then:
        var e = thrown(ThrowableProblem)
        e.status.code == 503


        when: "cron is hour ahead"
        MaintenanceProps mProps2 = new MaintenanceProps(
            crons: ["* * ${nyTime.plusHours(1).getHour()} * * MON-SUN".toString()],
            timeZone: tz
        )

        //should succeed with current time
        MaintWindowUtil.check(mProps2)

        then:
        noExceptionThrown()

    }

}
