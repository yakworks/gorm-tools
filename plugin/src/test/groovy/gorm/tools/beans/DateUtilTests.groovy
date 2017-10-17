package gorm.tools.beans

import java.text.SimpleDateFormat
import spock.lang.Specification

class DateUtilTests extends Specification {

    void testParseJsonDate() {
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

    void testGetLastDayOfMonth() {
        when:
        SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')
        Date date = format.parse('2011-09-11')
        def result = DateUtil.getLastDayOfMonth(date)

        then:
        format.parse('2011-09-30').format('yyyy-MM-dd') == result.format('yyyy-MM-dd')
    }

    void testIsTodayTheDate() {
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

    void testDaysBetween() {
        given:
        Date now = new Date()

        expect:
        0 == DateUtil.getDaysBetween(now, now)
        -10 == DateUtil.getDaysBetween(now - 10, now)
        10 == DateUtil.getDaysBetween(now + 10, now)
    }

}
