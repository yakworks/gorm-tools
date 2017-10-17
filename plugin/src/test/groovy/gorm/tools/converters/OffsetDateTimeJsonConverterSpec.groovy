package gorm.tools.converters

import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import spock.lang.Specification
import java.time.OffsetDateTime

@TestMixin(GrailsUnitTestMixin)
class OffsetDateTimeJsonConverterSpec extends Specification {

    Closure converter

    void setup() {
        converter = new OffsetDateTimeJsonConverter().getConverter()
    }

    void "test OffsetDateTime converter"() {
        setup:
        OffsetDateTime date = OffsetDateTime.parse("2017-10-13T10:54:30+01:00")

        when:
        String convertedDate = converter.call(date)

        then:
        "\"2017-10-13T10:54:30+01:00\"" == convertedDate
    }

    void "test type"() {
        expect:
        OffsetDateTime == converter.getType()
    }
}