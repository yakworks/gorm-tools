package gorm.tools.converters

import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import spock.lang.Specification
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.ZoneId

@TestMixin(GrailsUnitTestMixin)
class ZonedDateTimeJsonConverterSpec extends Specification {

    Closure converter

    void setup() {
        converter = new ZonedDateTimeJsonConverter().getConverter()
    }

    void "test ZonedDateTime converter"() {
        setup:
        LocalDateTime localDateTime = LocalDateTime.parse("2017-10-13T10:54:30")
        ZonedDateTime date = ZonedDateTime.of(localDateTime, ZoneId.of("Europe/Paris"))

        when:
        String convertedDate = converter.call(date)

        then:
        "\"2017-10-13T10:54:30+02:00[Europe/Paris]\"" == convertedDate
    }

    void "test type"() {
        expect:
        ZonedDateTime == converter.getType()
    }
}