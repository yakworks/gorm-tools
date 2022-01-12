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

    void "test ParseJsonDate"() {
        when:
        Date date = IsoDateUtil.parse('2000-03-30T22:00:00.000Z')
        then:
        '2000-03-30T22:00:00Z' == tester.format(date)

        when:
        date = IsoDateUtil.parse('2013-11-01T17:00:00Z')
        then:
        '2013-11-01T17:00:00Z' == tester.format(date)

        when:
        date = IsoDateUtil.parse('2013-11-01T23:00:00Z')
        then:
        '2013-11-01T23:00:00Z' == tester.format(date)
    }

    void "test ParseJsonDate multiple"() {
        expect:
        Date date = IsoDateUtil.parse(dateString)
        tester.format(date) == result

        where:
        dateString                 | result
        "2017-10-10"               | "2017-10-10T00:00:00Z"
        "2017-11-20T23:28:56.782Z" | "2017-11-20T23:28:56Z"
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

    void "test dateToString with default format"() {
        expect:
        "10/19/2017 12:00:00" == IsoDateUtil.dateToString(LocalDate.parse("2017-10-19").toDate())

        "10/19/2017 11:40:00" == IsoDateUtil.dateToString(LocalDateTime.parse("2017-10-19T11:40:00").toDate())

    }

    void "test dateToString"() {
        when:
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        Date date = dateFormat.parse('2017-10-19T11:50:10')

        then:
        "2017-10-19" == IsoDateUtil.dateToString(date, "yyyy-MM-dd")
        "10/19/2017 11:50:10" == IsoDateUtil.dateToString(date, "MM/dd/yyyy hh:mm:ss")
        "2017-10-19T11:50:10" == IsoDateUtil.dateToString(date, "yyyy-MM-dd'T'HH:mm:ss")
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

    void "test dateToJsonString"() {
        setup:
        SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = formatter.parse('2017-10-20 22:00:00')

        when:
        String result = IsoDateUtil.format(date)

        then:
        result == '2017-10-20T22:00:00.000Z'
    }

}
