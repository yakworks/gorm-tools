package gorm.tools.beans

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
        format.parse('2011-09-30').format('yyyy-MM-dd') == result.format('yyyy-MM-dd')
    }

    void "test addMonths"() {
        setup:
        SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')
        Date date = format.parse('2017-10-19')

        expect:
        '2017-07-19' == DateUtil.addMonths(date, -3).format('yyyy-MM-dd')
        '2017-12-19' == DateUtil.addMonths(date, 2).format('yyyy-MM-dd')
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
        def dayOfMonth = new Date().getAt(Calendar.DAY_OF_MONTH)
        def dayOfWeek = new Date().getAt(Calendar.DAY_OF_WEEK)

        then:
        DateUtil.isTodayTheDate('monthly', dayOfMonth)
        !DateUtil.isTodayTheDate('monthly', dayOfMonth - 1)

        DateUtil.isTodayTheDate('weekly', dayOfWeek)
        !DateUtil.isTodayTheDate('weekly', dayOfWeek - 1)

        DateUtil.isTodayTheDate('daily', 122)
        !DateUtil.isTodayTheDate('bzzz', dayOfWeek)
    }

    void "test DaysBetween"() {
        given:
        Date now = new Date()

        expect:
        0 == DateUtil.getDaysBetween(now, now)
        -10 == DateUtil.getDaysBetween(now - 10, now)
        10 == DateUtil.getDaysBetween(now + 10, now)
    }

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

    void "test dateToString with default format"() {
        expect:
        result == IsoDateUtil.dateToString(date)

        where:
        result                | date
        "10/19/2017 12:00:00" | new SimpleDateFormat("yyyy-MM-dd").parse("2017-10-19")
        "10/19/2017 11:40:00" | new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse("10/19/2017 11:40:00")
    }

    void "test dateToString"() {
        given:
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        Date date = dateFormat.parse('2017-10-19T11:50:10')

        expect:
        result == IsoDateUtil.dateToString(date, format)

        where:
        result                | format
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

    void "test getYearOf"() {
        expect:
        year == DateUtil.getYearOf(new SimpleDateFormat(dateFormat).parse(date))

        where:
        year | dateFormat     | date
        2017 | "yyyy-MM-dd"   | "2017-10-19"
        2016 | "MM/dd/yyyy"   | "10/19/2016"
        2015 | "yyMMddHHmmss" | "151019105000"
    }

    void "test shiftCurrentDateByMonths"() {
        setup:
        Calendar calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 2)

        when:
        Date monthAfterNow = DateUtil.shiftCurrentDateByMonths(2)

        then:
        monthAfterNow.format("yyyy-MM-dd") == calendar.getTime().format("yyyy-MM-dd")

        when:
        //changing expected date, so now it 2 month before now
        calendar.add(Calendar.MONTH, -4)
        Date monthBeforeNow = DateUtil.shiftCurrentDateByMonths(-2)

        then:
        monthBeforeNow.format("yyyy-MM-dd") == calendar.getTime().format("yyyy-MM-dd")
    }

    void "test getMonthLetter"() {
        expect:
        monthLetter == DateUtil.getMonthLetter(date)

        where:
        monthLetter | date
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
        Date date = new SimpleDateFormat(dateFormat).parse("10/19/2017 11:40:00.500")
        Calendar calendarDate = Calendar.getInstance()
        calendarDate.setTime(date)

        when:
        DateUtil.setTime(calendarDate, 10, 15)

        then:
        calendarDate.getTime().format(dateFormat) == "10/19/2017 10:15:00.000"

        when:
        DateUtil.setTime(calendarDate, 9, 30, 25, 123)

        then:
        calendarDate.getTime().format(dateFormat) == "10/19/2017 09:30:25.123"

        when:
        DateUtil.setTime(calendarDate, 23)

        then:
        calendarDate.getTime().format(dateFormat) == "10/19/2017 23:00:00.000"

        when:
        DateUtil.setTime(calendarDate, 0)

        then:
        calendarDate.getTime().format(dateFormat) == "10/19/2017 00:00:00.000"
    }

    void "test setTimeAsOfMidnight"() {
        setup:
        String dateFormat = "MM/dd/yyyy HH:mm:ss.SSS"
        Date date = new SimpleDateFormat(dateFormat).parse("10/19/2017 11:40:00.500")
        Calendar calendarDate = Calendar.getInstance()
        calendarDate.setTime(date)

        when:
        DateUtil.setTimeAsOfMidnight(calendarDate)

        then:
        calendarDate.getTime().format(dateFormat) == "10/19/2017 00:00:00.000"
    }

    void "test getCalendarInstanceByDate"() {
        setup:
        String dateFormat = "MM/dd/yyyy HH:mm:ss"
        Date date = new SimpleDateFormat(dateFormat).parse("10/19/2017 11:40:00")

        when:
        Calendar result = DateUtil.getCalendarInstanceByDate(date)

        then:
        result.getTime().format(dateFormat) == "10/19/2017 11:40:00"
    }

    void "test getCurrentCalendarInstance"() {
        setup:
        String dateFormat = "MM/dd/yyyy HH:mm:ss"
        Date date = new Date()

        when:
        Calendar result = DateUtil.getCurrentCalendarInstance()

        then:
        result.getTime().format(dateFormat) == date.format(dateFormat)
    }

}
