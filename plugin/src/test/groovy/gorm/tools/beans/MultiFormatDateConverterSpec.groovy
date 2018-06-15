/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.beans

import spock.lang.Specification

import java.text.SimpleDateFormat

class MultiFormatDateConverterSpec extends Specification {

    MultiFormatDateConverter converter

    void setup() {
        converter = new MultiFormatDateConverter()
    }

    void "test convert simple date"() {
        setup:
        SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')
        String date = "2017-10-13"

        when:
        Date resultDate = converter.convert(date)

        then:
        null != resultDate
        format.parse(date) == resultDate

    }

    void "test convert date with time"() {
        setup:
        SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss z')
        format.setTimeZone(TimeZone.getTimeZone('GMT'))
        String date = "2017-10-13T10:00:00.000Z"

        when:
        Date resultDate = converter.convert(date)

        then:
        null != resultDate
        "2017-10-13 10:00:00 GMT" == format.format(resultDate)

    }

    void "test convert empty date"() {
        setup:
        String date = ""

        when:
        Date resultDate = converter.convert(date)

        then:
        null == resultDate

    }

    void "test canConvert"() {
        setup:
        Object date = "2017-10-13"
        String date1 = "2017-10-13"
        Object object = new Object()

        expect:
        converter.canConvert(date)
        converter.canConvert(date1)
        !converter.canConvert(object)
    }

    void "test returnType"() {
        expect:
        Date == converter.getTargetType()
    }
}
