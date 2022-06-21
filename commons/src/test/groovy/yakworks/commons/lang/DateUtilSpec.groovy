/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.text.SimpleDateFormat

class DateUtilSpec extends Specification {
    @Shared SimpleDateFormat tester = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    void setupSpec(){
        tester.setTimeZone(TimeZone.getTimeZone('UTC'))
    }

    void "test getDateDifference_inHours if a date in the future"() {
        setup:
        Calendar calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1)
        //adding a second to be sure that there is still an hour between
        //this date and current date that is calculated in the method
        calendar.add(Calendar.SECOND, 1)

        when:
        Long hours = DateUtil.getDateDifference_inHours(calendar.getTime())

        then:
        1L == hours
    }

    void "test getDateDifference_inHours if a date in the past"() {
        setup:
        Calendar calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, -2)

        when:
        Long hours = DateUtil.getDateDifference_inHours(calendar.getTime())

        then:
        2L == hours
    }

    void "test convertStringToDateTime"() {
        setup:
        String format = "MM/dd/yyyy hh:mm:ss"
        String stringDate = "10/19/2017 11:50:10"
        SimpleDateFormat formatter = new SimpleDateFormat(format)
        Date expectedDate = formatter.parse(stringDate)

        when:
        Date result = DateUtil.convertStringToDateTime(stringDate, format)

        then:
        result == expectedDate
    }

}
