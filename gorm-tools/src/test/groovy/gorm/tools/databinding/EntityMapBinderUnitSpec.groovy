/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import groovy.transform.CompileStatic

import spock.lang.Ignore
import yakworks.commons.lang.IsoDateUtil
import gorm.tools.repository.model.RepoEntity
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.commons.model.IdEnum
import grails.databinding.converters.ValueConverter
import grails.persistence.Entity
import org.grails.databinding.converters.ConversionService
import org.grails.databinding.converters.DateConversionHelper
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink

import java.time.LocalDate
import java.time.LocalDateTime

class EntityMapBinderUnitSpec extends Specification implements DataRepoTest {
    EntityMapBinder binder

    void setup() {
        binder = new EntityMapBinder()
    }

    Class[] getDomainClassesToMock() {
        [TestDomain, Nest, AnotherDomain, BindableNested, KitchenSink]
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

    void "should fallback to conversion helpers2"() {
        setup:
        // ValueConverter currencyConverter = Mock(ValueConverter)
        // binder.conversionHelpers.put(Currency, [currencyConverter])

        TestDomain domain = new TestDomain()

        when:
        binder.bind(domain, [currency: "INR"])

        then:
        domain.currency == Currency.getInstance("INR")
    }

    void "should fallback to conversion service if no converversion helpers found"() {
        setup:
        def springConversionService = Mock(org.springframework.core.convert.ConversionService)
        binder.springConversionService  = springConversionService
        TestDomain domain = new TestDomain()

        when:
        binder.bind(domain, [currency: "INR"])

        then:
        1 * springConversionService.canConvert(_, _) >> true
        1 * springConversionService.convert("INR", _) >> Currency.getInstance("INR")

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


    void "bind association field ending in Id as String when it's blank"() {
        setup:
        TestDomain domain = new TestDomain()

        when: 'assigns null'
        binder.bind(domain, ["anotherDomainId": ""])

        then:
        domain
    }


    void "bind association field ending in Id as String"() {
        setup:
        TestDomain domain = new TestDomain()
        def assoc = new AnotherDomain(id: 1, name: "test").persist(flush: true)

        when: 'assigns a new id'
        binder.bind(domain, ["anotherDomainId": "1"])

        then:
        domain.anotherDomain == assoc
    }

    void "bind association field with map id key as string"() {
        setup:
        TestDomain domain = new TestDomain()
        def assoc = new AnotherDomain(id: 1, name: "test").persist(flush: true)

        when: 'assigns a new id'
        binder.bind(domain, [anotherDomain:[id: "1"]])

        then:
        domain.anotherDomain == assoc
    }

    void "bind association field ending in Id"() {
        setup:
        TestDomain domain = new TestDomain()
        def assoc = new AnotherDomain(id: 1, name: "test").persist(flush: true)
        def assoc2 = new AnotherDomain(id: 2, name: "test2").persist(flush: true)

        Map params = ["anotherDomainId": 1]

        when: 'assigns a new id'
        binder.bind(domain, ["anotherDomainId": 1])

        then:
        domain.anotherDomain == assoc

        when: 'assigns a different id'
        binder.bind(domain, ["anotherDomainId": 2])

        then:
        domain.anotherDomain == assoc2
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

    void "test bind boolean val"() {
        TestDomain testDomain = new TestDomain()
        Map params = [active: true]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.active == true

        when:
        params = [active: false]
        binder.bind(testDomain, params)

        then:
        testDomain.active == false

        when:
        params = [active: 0]
        binder.bind(testDomain, params)

        then:
        testDomain.active == false

        when:
        params = [active: 1]
        binder.bind(testDomain, params)

        then:
        testDomain.active == true
    }

    void "test bind BigDecimal"() {
        TestDomain testDomain = new TestDomain()
        Map params = [amount: '99.999']

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.amount == 99.999

        when:
        params = [amount: 99.999]
        binder.bind(testDomain, params)

        then:
        testDomain.amount == 99.999

        when:
        params = [amount: 99]
        binder.bind(testDomain, params)

        then:
        testDomain.amount == 99.00

        when:
        params = [amount: '99']
        binder.bind(testDomain, params)

        then:
        testDomain.amount == 99.00
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

    void "set prop to null"() {
        given:
        TestDomain td = new TestDomain(name: 'bill', age: 100)
        assert td.name != null
        assert td.age == 100
        Map params = [name: null, age: null]

        when:
        binder.bind(td, params)

        then:
        td.name == null
        td.age == null
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

    void "binder should not create new association if its reference is not null"() {
        TestDomain testDomain = new TestDomain()
        Nest nested = new Nest(name2:"xxxx")
        testDomain.nested = nested
        Map params = [name: 'test', nested:[name:"test"]]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == "test"
        testDomain.nested != null
        testDomain.nested.is(nested)
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

    void "binder should set reference if value is of association type"() {
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

    void "binder should create new association if constraints contain explicit bindable:true"() {
        TestDomain testDomain = new TestDomain()
        Map params = [name: 'outer', notBindable: 'notBindableTest', bindableNested: [name: 'bindableNested'],
                      notBindableNested: [name: 'notBindableNested']]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == 'outer'
        testDomain.notBindable == null

        testDomain.bindableNested != null
        testDomain.bindableNested.name == 'bindableNested'

        testDomain.notBindableNested == null
    }

    void "will bind association even if id does not exist"() {
        TestDomain testDomain = new TestDomain()
        Map params = [name: 'outer',
                      notBindableNested: [id: 99999]]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == 'outer'
        testDomain.notBindableNested.id == 99999
    }

    void "should not bind non bindable props of nested domains"() {
        TestDomain testDomain = new TestDomain(nested: new Nest(name:"name1", name2: "name2"))

        Map params = [nested: [name: "updated", name2:"updated"]]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.nested.name == "updated"
        testDomain.nested.name2 == "name2" //its bindable:false
    }

    void "binder shouldn't bind the association if constraints doesn't contain 'bindable' and it does not belongsTo"() {
        TestDomain testDomain = new TestDomain()
        Map params = [name: 'outer', notBindable: 'notBindableTest', notBindableNested: [name: 'notBindableNested']]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.name == 'outer'
        //regular field
        testDomain.notBindable == null

        //association
        testDomain.notBindableNested == null
    }

    void "binder shouldn't initialize proxy when checks association's id"() {
        BindableNested nested = new BindableNested(name: 'proxy').save(failOnError: true)
        TestDomain testDomain = new TestDomain(notBindableNested: nested, nested: new Nest(name:"nested-belongsTo")).save(failOnError: true)

        Map params = [name: 'test', notBindableNested: [id: nested.id, name: 'nested']]

        expect:
        // clearing the session to get TestDomain entity with a proxy for 'nested' property
        flushAndClear()
        TestDomain testDomainWithProxy = TestDomain.get(testDomain.id)

        when:
        binder.bind(testDomainWithProxy, params)

        then:
        // class names are not equal, because testDomainWithProxy.nested is a proxy and it has an appropriate class name,
        // which differs from 'gorm.tools.databinding.Nested'
        testDomainWithProxy.notBindableNested.getClass().name != testDomain.notBindableNested.getClass().name

        // 'nested' property isn't initialized
        !GrailsHibernateUtil.isInitialized(testDomainWithProxy, 'notBindableNested')

        when:
        Long nestedId = testDomainWithProxy.nested.id

        then: "getting id shouldn't initialize the proxy"
        !GrailsHibernateUtil.isInitialized(testDomainWithProxy, 'notBindableNested')
        nestedId == nested.id

        when:
        String nestedName = testDomainWithProxy.notBindableNested.name

        then: "getting name initializes the proxy"
        GrailsHibernateUtil.isInitialized(testDomainWithProxy, 'notBindableNested')
        nestedName == 'proxy'
    }

    void "test enums"() {
        given:

        TestDomain testDomain = new TestDomain()
        Map params = [testEnum: "FOO", enumSub:"BAR"]

        when:
        binder.bind(testDomain, params)

        then:
        testDomain.testEnum == TestEnum.FOO
        testDomain.enumSub == TestDomain.EnumSub.BAR
    }

    void "test enums identity"() {
        given:

        TestDomain testDomain = new TestDomain()
        Map params = [enumIdent: 2]

        when:
        binder.bind(testDomain, params)

        then:
        TestEnumIdent.get(2) == TestEnumIdent.Num2
        testDomain.enumIdent == TestEnumIdent.Num2
    }

    void "test enums identity from map"() {
        given:

        TestDomain testDomain = new TestDomain()
        Map params = [enumIdent: [id:2]]

        when:
        binder.bind(testDomain, params)

        then:
        TestEnumIdent.get(2) == TestEnumIdent.Num2
        testDomain.enumIdent == TestEnumIdent.Num2
    }

    void "test binder should convert empty values to null"() {
        given:
        TestDomain testDomain = new TestDomain()
        Map params = [name: "  ", age: "", amount: "", localDate: "", active: "  "]

        when:
        binder.bind(testDomain, params)

        then: "No exceptions or class cast errors should have been generates, empty values set as null"
        noExceptionThrown()

        testDomain.hasErrors() == false
        testDomain.name == null
        testDomain.age == null
        testDomain.amount == null
        testDomain.localDate == null
        testDomain.active == null
    }

}


@Entity
class TestDomain implements RepoEntity<TestDomain> {
    // by default binder consider regular fields as "bindable:true",
    // so there is no need to specify constraints explicitly for that
    String name
    String notBindable
    Long age
    BigDecimal amount
    Date dobDate
    LocalDate localDate
    LocalDateTime localDateTime
    Currency currency
    Boolean active

    AnotherDomain anotherDomain
    Nest nested

    TestEnum testEnum
    EnumSub enumSub
    TestEnumIdent enumIdent

    // constraints contain explicit "bindable:true"
    BindableNested bindableNested
    // constraints doesn't contain bindable property and there is no cascading stuff between TestDomain and BindableNested,
    // thus this association should not be binded by map binder.
    BindableNested notBindableNested

    static constraints = {
        notBindable bindable: false
        nested nullable: false
        anotherDomain nullable: true
        bindableNested bindable:true
    }
    static mapping = {
        enumIdent enumType: 'identity'
    }

    enum EnumSub {FOO, BAR}

}

enum TestEnum {FOO, BAR}

@CompileStatic
enum TestEnumIdent implements IdEnum<TestEnumIdent,Long>{
    Num2(2), Num4(4)
    final Long id

    TestEnumIdent(Long id) { this.id = id }

    // static final Map map = values().collectEntries { [(it.id): it]} as TreeMap
    //
    // static TestEnumIdent get(Long id) {
    //     return map[id]
    // }
}

@Entity
class AnotherDomain implements RepoEntity<AnotherDomain>{
    String name
    String source
}

@Entity
class Nest implements RepoEntity<Nest>{
    String name
    String name2

    static belongsTo = [TestDomain]

    static constraints = {
        name nullable: false
        name2 nullable: true, bindable:false
    }
}

@Entity
class BindableNested implements RepoEntity<BindableNested>{
    String name
}
