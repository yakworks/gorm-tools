package gorm.tools.converters

import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import spock.lang.Specification
import java.time.OffsetTime

@TestMixin(GrailsUnitTestMixin)
class OffsetTimeJsonConverterSpec extends Specification {

    Closure converter

    void setup() {
        converter = new OffsetTimeJsonConverter().getConverter()
    }

    void "test OffsetTime converter"() {
        setup:
        OffsetTime date = OffsetTime.parse("10:54:30+01:00")

        when:
        String convertedDate = converter.call(date)

        then:
        "\"10:54:30+01:00\"" == convertedDate
    }

    void "test type"() {
        expect:
        OffsetTime == converter.getType()
    }
}