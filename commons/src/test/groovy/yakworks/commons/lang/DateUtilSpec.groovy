/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import spock.lang.Unroll

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

    void "test getLastDayOfMonth"() {
        when:
        SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')
        Date date = format.parse('2011-09-11')
        Date result = DateUtil.getLastDayOfMonth(date)

        then:
        '2011-09-30' == format.format(result)
    }

    void "test addMonths"() {
        setup:
        SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')
        Date date = format.parse('2017-10-19')

        expect:
        '2017-07-19' == format.format(DateUtil.addMonths(date, -3))
        '2017-12-19' == format.format(DateUtil.addMonths(date, 2))
    }

    void "test getFirstDayOfMonth"() {
        setup:
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        Date date = format.parse("10/19/2017 11:40:00")

        when:
        Date result = DateUtil.getFirstDayOfMonth(date)

        then:
        format.parse('10/01/2017 00:00:00') == result
    }

    void "test IsTodayTheDate"() {
        when:
        def locDate = LocalDate.now()
        def dayOfMonth = locDate.getDayOfMonth()
        def dayOfWeek = DayOfWeek.from(locDate).value

        then:
        DateUtil.isTodayTheDate(ChronoUnit.MONTHS, dayOfMonth)
        !DateUtil.isTodayTheDate(ChronoUnit.MONTHS, dayOfMonth - 1)

        DateUtil.isTodayTheDate(ChronoUnit.WEEKS, dayOfWeek)
        !DateUtil.isTodayTheDate(ChronoUnit.WEEKS, dayOfWeek - 1)

        DateUtil.isTodayTheDate(ChronoUnit.DAYS, 122)
    }

    // @Ignore //FIXME this is blowing up on daylight savings today. Adding 10 thinks it 9 days apart.
    // void "test DaysBetween"() {
    //     given:
    //     Date now = new Date()
    //
    //     expect:
    //     0 == DateUtil.getDaysBetween(now, now)
    //     -10 == DateUtil.getDaysBetween(now - 10, now)
    //     10 == DateUtil.getDaysBetween(now + 10, now)
    // }

    void "test getMonthDiff"() {
        given:
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd")
        Date date1 = format.parse("2017-10-19")
        Date date2 = format.parse("2017-12-19")

        expect:
        2 == DateUtil.getMonthDiff(date1, date2)

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

    @Unroll
    void "test dateToString with default format"(String r, Date date) {
        expect:
        r == IsoDateUtil.dateToString(date)

        where:
        r                     | date
        "10/19/2017 12:00:00" | new SimpleDateFormat("yyyy-MM-dd").parse("2017-10-19")
        "10/19/2017 11:40:00" | new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse("10/19/2017 11:40:00")
    }

    @Unroll
    void "test dateToString"(String r, String fmt) {
        setup:
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        Date date = dateFormat.parse('2017-10-19T11:50:10')

        expect:
        r == IsoDateUtil.dateToString(date, fmt)

        where:
        r                     | fmt
        "2017-10-19"          | "yyyy-MM-dd"
        "10/19/2017 11:50:10" | "MM/dd/yyyy hh:mm:ss"
        "2017-10-19T11:50:10" | "yyyy-MM-dd'T'HH:mm:ss"
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

    @Unroll
    void "test getYearOf"(int year, String fmt, String dt) {
        expect:
        year == DateUtil.getYearOf(new SimpleDateFormat(fmt).parse(dt))

        where:
        year | fmt     | dt
        2017 | "yyyy-MM-dd"   | "2017-10-19"
        2016 | "MM/dd/yyyy"   | "10/19/2016"
        2015 | "yyMMddHHmmss" | "151019105000"
    }

    @Unroll
    void "test getYearOf with LocalDates"(int year, String fmt, String dt) {
        expect:
        year == DateUtil.getYearOf(DateUtil.toLocalDate(new SimpleDateFormat(fmt).parse(dt)))

        where:
        year | fmt     | dt
        2017 | "yyyy-MM-dd"   | "2017-10-19"
        2016 | "MM/dd/yyyy"   | "10/19/2016"
        2015 | "yyMMddHHmmss" | "151019105000"
    }

    void "test shiftCurrentDateByMonths"() {
        setup:
        Calendar calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 2)
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd")

        when:
        Date monthAfterNow = DateUtil.shiftCurrentDateByMonths(2)

        then:
        fmt.format(monthAfterNow) == fmt.format(calendar.getTime())

        when:
        //changing expected date, so now it 2 month before now
        calendar.add(Calendar.MONTH, -4)
        Date monthBeforeNow = DateUtil.shiftCurrentDateByMonths(-2)

        then:
        fmt.format(monthBeforeNow) == fmt.format(calendar.getTime())
    }

    @Unroll
    void "test getMonthLetter"(String monthLetter, String dt) {
        expect:
        monthLetter == DateUtil.getMonthLetter(dt)

        where:
        monthLetter | dt
        "J"         | "201701"
        "F"         | "201702"
        "M"         | "201703"
        "A"         | "201704"
        "M"         | "201705"
        "J"         | "201706"
        "J"         | "201707"
        "A"         | "201708"
        "S"         | "201709"
        "O"         | "201710"
        "N"         | "201711"
        "D"         | "201712"
        "?"         | "201713"
        "?"         | ""
    }

    void "test getMonthLetterByNum"() {
        expect:
        monthLetter == DateUtil.getMonthLetterByNum(monthNumber)

        where:
        monthLetter | monthNumber
        "J"         | 1
        "F"         | 2
        "M"         | 3
        "A"         | 4
        "M"         | 5
        "J"         | 6
        "J"         | 7
        "A"         | 8
        "S"         | 9
        "O"         | 10
        "N"         | 11
        "D"         | 12
        "?"         | 13
        "?"         | -1
    }

    void "test setTime"() {
        setup:
        String dateFormat = "MM/dd/yyyy HH:mm:ss.SSS"
        SimpleDateFormat fmt =  new SimpleDateFormat(dateFormat)
        Date date = fmt.parse("10/19/2017 11:40:00.500")
        Calendar calendarDate = Calendar.getInstance()
        calendarDate.setTime(date)

        when:
        DateUtil.setTime(calendarDate, 10, 15)

        then:
        fmt.format(calendarDate.getTime()) == "10/19/2017 10:15:00.000"

        when:
        DateUtil.setTime(calendarDate, 9, 30, 25, 123)

        then:
        fmt.format(calendarDate.getTime()) == "10/19/2017 09:30:25.123"

        when:
        DateUtil.setTime(calendarDate, 23)

        then:
        fmt.format(calendarDate.getTime()) == "10/19/2017 23:00:00.000"

        when:
        DateUtil.setTime(calendarDate, 0)

        then:
        fmt.format(calendarDate.getTime()) == "10/19/2017 00:00:00.000"
    }

    void "test setTimeAsOfMidnight"() {
        setup:
        String dateFormat = "MM/dd/yyyy HH:mm:ss.SSS"
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat)
        Date date = fmt.parse("10/19/2017 11:40:00.500")
        Calendar calendarDate = Calendar.getInstance()
        calendarDate.setTime(date)

        when:
        DateUtil.setTimeAsOfMidnight(calendarDate)

        then:
        fmt.format(calendarDate.getTime()) == "10/19/2017 00:00:00.000"
    }

    void "test getCalendarInstanceByDate"() {
        setup:
        String dateFormat = "MM/dd/yyyy HH:mm:ss"
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat)
        Date date = fmt.parse("10/19/2017 11:40:00")

        when:
        Calendar result = DateUtil.getCalendarInstanceByDate(date)

        then:
        fmt.format(result.getTime()) == "10/19/2017 11:40:00"
    }

    void "test getCurrentCalendarInstance"() {
        setup:
        String dateFormat = "MM/dd/yyyy HH:mm:ss"
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat)
        Date date = new Date()

        when:
        Calendar result = DateUtil.getCurrentCalendarInstance()

        then:
        fmt.format(result.getTime()) == fmt.format(date)
    }

}
