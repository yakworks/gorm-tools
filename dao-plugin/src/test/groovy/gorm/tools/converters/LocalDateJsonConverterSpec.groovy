package gorm.tools.converters

import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import spock.lang.Specification
import java.time.LocalDate

@TestMixin(GrailsUnitTestMixin)
class LocalDateJsonConverterSpec extends Specification {

    Closure converter

    void setup() {
        converter = new LocalDateJsonConverter().getConverter()
    }

    void "test LocalDate converter"() {
        setup:
        LocalDate date = LocalDate.parse("2017-10-13")

        when:
        String convertedDate = converter.call(date)

        then:
        "\"2017-10-13\"" == convertedDate
    }

    void "test type"() {
        expect:
        LocalDate == converter.getType()
    }
}