package gorm.tools.beans

import java.text.SimpleDateFormat
import spock.lang.Specification

class DateUtilSpec extends Specification {

    void "test ParseJsonDate"() {
        when:
        SimpleDateFormat tester = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss z')
        tester.setTimeZone(TimeZone.getTimeZone('GMT'))
        Date date = DateUtil.parseJsonDate('2000-03-30T22:00:00.000Z')
        then:
        '2000-03-30 22:00:00 GMT' == tester.format(date)

        when:
        date = DateUtil.parseJsonDate('2013-11-01T17:00:00Z')
        then:
        '2013-11-01 17:00:00 GMT' == tester.format(date)

        when:
        date = DateUtil.parseJsonDate('2013-11-01T23:00:00Z')
        then:
        '2013-11-01 23:00:00 GMT' == tester.format(date)
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
        !DateUtil.isTodayTheDate('monthly', dayOfMonth-1)

        DateUtil.isTodayTheDate('weekly', dayOfWeek)
        !DateUtil.isTodayTheDate('weekly', dayOfWeek-1)

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
        result == DateUtil.dateToString(date)

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
        result == DateUtil.dateToString(date, format)

        where:
        result                | format
        "2017-10-19"          | "yyyy-MM-dd"
        "10/19/2017 11:50:10" | "MM/dd/yyyy hh:mm:ss"
        "2017-10-19T11:50:10" | "yyyy-MM-dd'T'HH:mm:ss"
    }

    void "test getYearOf"() {
        expect:
        year == DateUtil.getYearOf(new SimpleDateFormat(dateFormat).parse(date))

        where:
        year   | dateFormat     | date
        2017   | "yyyy-MM-dd"   | "2017-10-19"
        2016   | "MM/dd/yyyy"   | "10/19/2016"
        2015   | "yyMMddHHmmss" | "151019105000"
    }

    void "test shiftCurrentDateByMonths"() {
        setup:
        Calendar calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 1)

        when:
        Date date = DateUtil.shiftCurrentDateByMonths(1)

        then:
        date.format("yyyy-MM-dd") == calendar.getTime().format("yyyy-MM-dd")
    }

}
