/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.commons.lang.DateUtil
import yakworks.commons.lang.IsoDateUtil

import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class LocalDateUtilsSpec extends Specification {
    @Shared SimpleDateFormat tester = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    void setupSpec(){
        tester.setTimeZone(TimeZone.getTimeZone('UTC'))
    }

    void "test getLastDayOfMonth"() {
        expect:
        '2021-02-28' == LocalDateUtils.getLastDateOfMonth(LocalDate.parse('2021-02-01')).toString()

    }

    void "test addMonths"() {
        setup:
        Date perpost = LocalDate.parse('2017-10-19').toDate()

        expect:
        '2017-07-19' == DateUtil.addMonths(perpost, -3).toLocalDate().toString()
        '2017-12-19' == DateUtil.addMonths(perpost, 2).toLocalDate().toString()
    }

    void "test getYearOf"() {
        expect:
        2017 == DateUtil.getYearOf(LocalDate.parse("2017-10-19"))
    }

    void "test getMonthLetter"() {
        expect:
        monthLetter == DateUtil.getMonthLetter(x)

        where:
        monthLetter | x
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

}
