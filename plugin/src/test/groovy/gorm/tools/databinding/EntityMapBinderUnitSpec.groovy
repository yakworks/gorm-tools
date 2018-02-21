package gorm.tools.databinding

import gorm.tools.beans.IsoDateUtil
import gorm.tools.testing.unit.DataRepoTest
import grails.databinding.converters.ValueConverter
import grails.persistence.Entity
import grails.testing.gorm.DataTest
import org.grails.databinding.converters.ConversionService
import org.grails.databinding.converters.DateConversionHelper
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EntityMapBinderUnitSpec extends Specification implements DataRepoTest {
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
        domain.bind([age: "100"])

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
        binder.bind(domain, [dobDate: "2017-10-10", localDate: "2017-10-10", localDateTime: "2017-11-22"])

        then:
        0 * dateConverter.canConvert(_)
        domain.dobDate == IsoDateUtil.parse("2017-10-10")
        domain.localDate == LocalDate.parse("2017-10-10")
        domain.localDateTime == LocalDateTime.parse("2017-11-22T00:00") //LocalDateTime.parse(row['date3'] as String, DateTimeFormatter.ISO_DATE_TIME)
        //'2017-11-22T23:28:56.782Z'

        when:
        def isoDateZ = "2017-11-22T22:22:22.222Z"
        binder.bind(domain, [dobDate: isoDateZ,
                             localDate: isoDateZ,
                             localDateTime: isoDateZ])

        then:
        domain.dobDate == IsoDateUtil.parse(isoDateZ)
        domain.localDate == LocalDate.parse("2017-11-22")
        domain.localDateTime == LocalDateTime.parse("2017-11-22T22:22:22.222")

        when:
        def isoDateNoTZ = "2017-11-22T22:22"
        binder.bind(domain, [dobDate: isoDateNoTZ,
                             localDate: isoDateNoTZ,
                             localDateTime: isoDateNoTZ])

        then:
        domain.dobDate == IsoDateUtil.parse(isoDateNoTZ)
        domain.localDate == LocalDate.parse("2017-11-22")
        domain.localDateTime == LocalDateTime.parse(isoDateNoTZ)
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

    void "test trimStrings and convertEmptyStringsToNull"() {
        given:
        TestDomain testDomain = new TestDomain()
        Map params = [name: " test "]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == "test"

        when:
        binder.bind(testDomain, [name: "   "])

        then:
        testDomain.name == null
    }

    void "test default whitelist"() {
        given:
        TestDomain testDomain = new TestDomain()
        Map params = [name: 'bill', notBindable: 'got it']

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == 'bill'
        testDomain.notBindable == null

        when:
        testDomain = new TestDomain()
        binder.bind(testDomain, params, include: ['notBindable'])

        then:
        testDomain.name == null
        testDomain.notBindable == "got it"
    }

    void "test type conversion errors"() {
        TestDomain testDomain = new TestDomain()
        Map params = [age: 'test']

        when:
        binder.bind(testDomain, params)

        then:
        noExceptionThrown()
        testDomain.errors.errorCount == 1
        testDomain.errors.hasFieldErrors('age')
    }

    void "binder should create new association if it belongsTo"() {
        TestDomain testDomain = new TestDomain()
        Map params = [name: 'test', nested:[name:"test"]]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == "test"
        testDomain.nested != null
        testDomain.nested.name == "test"
    }

    void "binder should load existing association if it does not belongsTo"() {
        TestDomain testDomain = new TestDomain()
        AnotherDomain anotherDomain = new AnotherDomain(id:1, name:"name").save()
        Map params = [name: 'test', anotherDomain:[id:1, name:"test"]]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == "test"
        testDomain.anotherDomain != null
        testDomain.anotherDomain == anotherDomain
        testDomain.anotherDomain.name != "test" //should not be deep bound if does not belogsTo
    }

    void "binder should set reference if value if of association type"() {
        TestDomain testDomain = new TestDomain()
        AnotherDomain anotherDomain = new AnotherDomain(id:1, name:"name").save()
        Map params = [name: 'test', anotherDomain:anotherDomain]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == "test"
        testDomain.anotherDomain != null
        testDomain.anotherDomain == anotherDomain
        testDomain.anotherDomain.name == "name"
    }
}


@Entity
class TestDomain {
    String name
    String notBindable
    Long age
    Date dobDate
    LocalDate localDate
    LocalDateTime localDateTime
    Currency currency
    Boolean active

    AnotherDomain anotherDomain
    Nested nested

    static constraints = {
        notBindable bindable: false
        nested nullable: false
        anotherDomain nullable: true
    }
}

@Entity
class AnotherDomain {
    String name
}

@Entity
class Nested {
    String name

    static belongsTo = [TestDomain]

    static constraints = {
        name nullable: false
    }
}
