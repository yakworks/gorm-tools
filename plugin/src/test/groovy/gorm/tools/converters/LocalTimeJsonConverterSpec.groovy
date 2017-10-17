package gorm.tools.converters

import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import spock.lang.Specification
import java.time.LocalTime

@TestMixin(GrailsUnitTestMixin)
class LocalTimeJsonConverterSpec extends Specification {

    Closure converter

    void setup() {
        converter = new LocalTimeJsonConverter().getConverter()
    }

    void "test LocalTime converter"() {
        setup:
        LocalTime date = LocalTime.parse("10:54:30")

        when:
        String convertedDate = converter.call(date)

        then:
        "\"10:54:30\"" == convertedDate
    }

    void "test type"() {
        expect:
        LocalTime == converter.getType()
    }
}