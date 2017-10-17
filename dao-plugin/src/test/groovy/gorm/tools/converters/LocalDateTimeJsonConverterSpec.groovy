package gorm.tools.converters

import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import spock.lang.Specification
import java.time.LocalDateTime

@TestMixin(GrailsUnitTestMixin)
class LocalDateTimeJsonConverterSpec extends Specification {

    Closure converter

    void setup() {
        converter = new LocalDateTimeJsonConverter().getConverter()
    }

    void "test LocalDateTime converter"() {
        setup:
        LocalDateTime date = LocalDateTime.parse("2017-10-13T10:54:30")

        when:
        String convertedDate = converter.call(date)

        then:
        "\"2017-10-13T10:54:30\"" == convertedDate
    }

    void "test type"() {
        expect:
        LocalDateTime == converter.getType()
    }
}