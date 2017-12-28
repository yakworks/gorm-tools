package gorm.tools.databinding

import gorm.tools.beans.IsoDateUtil
import grails.databinding.converters.ValueConverter
import grails.gorm.annotation.Entity
import grails.testing.gorm.DataTest
import org.grails.databinding.converters.ConversionService
import org.grails.databinding.converters.DateConversionHelper
import spock.lang.Ignore
import spock.lang.Specification

class EntityMapBinderUnitSpec extends Specification implements DataTest {
    EntityMapBinder binder

    void setup() {
        binder = new EntityMapBinder()
    }

    Class[] getDomainClassesToMock() {
        [TestDomain, AnotherDomain]
    }

    void "should bind numbers without going through converters"() {
        setup:
        ValueConverter longConverter = Mock(ValueConverter)
        binder.conversionHelpers.put(Long, [longConverter])
        binder.conversionService = Mock(ConversionService)

        TestDomain domain = new TestDomain()

        when:
        binder.bind(domain, [age: "100"])

        then:
        0 * longConverter.canConvert(_)
        0 * binder.conversionService.canConvert(_, _)
        domain.age == 100L
    }

    void "should bind date without going through converters"() {
        setup:
        DateConversionHelper dateConverter = Mock(DateConversionHelper)
        binder.conversionHelpers.put(Date, [dateConverter])

        TestDomain domain = new TestDomain()

        when:
        binder.bind(domain, [dob: "2017-10-10"])

        then:
        0 * dateConverter.canConvert(_)
        domain.dob == IsoDateUtil.parse("2017-10-10")
    }


    void "should fallback to conversion helpers"() {
        setup:
        ValueConverter currencyConverter = Mock(ValueConverter)
        binder.conversionHelpers.put(Currency, [currencyConverter])

        TestDomain domain = new TestDomain()

        when:
        binder.bind(domain, [currency: "INR"])

        then:
        1 * currencyConverter.canConvert("INR") >> true
        1 * currencyConverter.convert("INR") >> Currency.getInstance("INR")

        domain.currency == Currency.getInstance("INR")
    }

    @Ignore //TODO Fix
    void "should fallback to conversion service if no converversion helpers found"() {
        setup:
        ConversionService conversionService = Mock(ConversionService)
        binder.conversionService = conversionService
        TestDomain domain = new TestDomain()

        when:
        binder.bind(domain, [currency: "INR"])

        then:
        1 * conversionService.canConvert(_, _) >> true
        1 * conversionService.convert("INR", _) >> Currency.getInstance("INR")

        domain.currency == Currency.getInstance("INR")
    }

    void "bind association"() {
        setup:
        TestDomain domain = new TestDomain()
        AnotherDomain assoc = new AnotherDomain(id: 1, name: "test").save(failOnError: true, flush: true)
        Map params = ["anotherDomain": [id: 1]]
        when:
        binder.bind(domain, params)

        then:
        domain.anotherDomain == assoc
    }

    void "test bind boolean"() {
        TestDomain testDomain = new TestDomain()
        Map params = [active: "true"]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.active == true

        when:
        params = [active: "false"]
        binder.bind(testDomain, params)

        then:
        testDomain.active == false

        when:
        params = [active: "on"]
        binder.bind(testDomain, params)

        then:
        testDomain.active == true
    }

}


@Entity
class TestDomain {
    String name
    Long age
    Date dob
    Currency currency
    Boolean active

    AnotherDomain anotherDomain
}

@Entity
class AnotherDomain {
    String name
}
