/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

import org.springframework.scheduling.support.CronExpression

import gorm.tools.model.SourceType
import spock.lang.Specification
import yakworks.json.groovy.JsonEngine
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.SecurityTest

class CronSandboxSpec extends Specification {

    void "kick off simulation of Job"() {
        when:
        var expression = CronExpression.parse("* * 21-23 * * MON-FRI");
        Temporal result = expression.next(LocalDateTime.now());
        String days = expression.@fields[0].toString()

        then:
        days
        expression
        result
    }

    void "parse days of week"() {
        when:
        var expression = CronExpression.parse("* * 14-15 * * MON-FRI");
        var curDate = LocalDateTime.parse("2023-09-20T14:00") //a wednesday
        LocalDateTime nextRun = expression.next(curDate)
        long secDif = ChronoUnit.SECONDS.between(curDate, nextRun)
        assert secDif == 1

        //Temporal result = expression.next(LocalDateTime.now());
        String cronDayOfWeekString = expression.@fields[0].toString()
        String cronHourOfDayString = expression.@fields[3].toString()
        assert cronDayOfWeekString == "DayOfWeek {1, 2, 3, 4, 5}"
        assert cronDayOfWeekString[11..-2] == "1, 2, 3, 4, 5"
        List cronDayList = cronDayOfWeekString[11..-2].tokenize(",").collect{it.trim() as int}
        assert cronHourOfDayString == "HourOfDay {14, 15}"
        assert cronHourOfDayString[11..-2] == "14, 15"
        List cronHourList = cronHourOfDayString[11..-2].tokenize(",").collect{it.trim() as int}

        then:
        cronDayList[0] instanceof Integer
        cronDayList == [1,2,3,4,5]
        cronHourList == [14,15]

        when:
        DayOfWeek dayOfWeek = curDate.dayOfWeek
        int hourOfDay = curDate.hour

        then:
        hourOfDay == 14
        dayOfWeek == DayOfWeek.WEDNESDAY
        dayOfWeek.value == 3
        cronDayList.contains(dayOfWeek.value)
        cronHourList.contains(hourOfDay)
    }

    void "check using next"() {
        when:
        //should be every second
        var expression = CronExpression.parse("* * 14,15 * * MON-FRI");
        var curDate = LocalDateTime.parse("2023-09-20T13:59:59")//a wednesday
        LocalDateTime nextRun = expression.next(curDate);
        long secDif = ChronoUnit.SECONDS.between(curDate, nextRun);

        then: "if its in range then will be 1"
        secDif == 1

        when: "1 min before"
        curDate = LocalDateTime.parse("2023-09-20T13:59:00")//a wednesday
        nextRun = expression.next(curDate);
        secDif = ChronoUnit.SECONDS.between(curDate, nextRun);

        then:
        secDif == 60

        when: "in middle"
        curDate = LocalDateTime.parse("2023-09-20T15:00:00")//a wednesday
        nextRun = expression.next(curDate);
        secDif = ChronoUnit.SECONDS.between(curDate, nextRun);

        then:
        secDif == 1

        when: "at end"
        curDate = LocalDateTime.parse("2023-09-20T16:00")//a wednesday
        nextRun = expression.next(curDate);
        secDif = ChronoUnit.SECONDS.between(curDate, nextRun);

        then:
        secDif == 79200
    }
}
