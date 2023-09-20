/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job


import java.time.LocalDateTime

import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem

class MaintWindowUtilSpec extends Specification {

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
        List<String> cronList = ['* * 14,15 * * MON-FRI', '* * 20,21,22,23 * * SAT,SUN']

        // NOTE: 2023-09-20 is a WED. 2023-09-23 is SAT
        then:
        //1 == MaintWindowUtil.secondsToNextRun(cron, LocalDateTime.parse("2023-09-20T13:59:59"))
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T13:59:00"))
        // at end
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T16:00"))

        when: "at start m-f"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T13:59:59"))

        then:
        var e = thrown(ThrowableProblem)
        e.status.code == 503

        when: "im middle m-f"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T15:00:00"))

        then:
        e = thrown(ThrowableProblem)
        e.status.code == 503

        when: "at end m-f"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-20T16:00:00"))

        then:
        noExceptionThrown()

        when: "Sat at M-F time"
        //make sure the first one doesnt pick up and only the weekend one picks up
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-23T15:00:00"))

        then:
        noExceptionThrown()

        when: "Sat at 3-7 start time"
        MaintWindowUtil.checkWindows(cronList, LocalDateTime.parse("2023-09-23T20:00:00"))

        then:
        e = thrown(ThrowableProblem)
        e.status.code == 503

    }
}
