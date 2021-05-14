/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import gorm.tools.beans.domain.*
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import testing.Address
import testing.Nested
import testing.Cust
import testing.CustType
import testing.TestSeedData

class EntityMapServiceSpec extends Specification implements DataRepoTest {

    EntityMapService entityMapService = new EntityMapService()

    void setupSpec() {
        //mockDomain Person
        mockDomains Bookz, BookTag, BookAuthor, EnumThing, Cust, Address, CustType, Nested
    }

    void "test buildIncludesMap"(){
        when:
        def res = entityMapService.buildIncludesMap("Bookz", ['name'])

        then:
        res.className == 'Bookz' // [className: 'Bookz', props: ['name']]
        res.fields == ['name'] as Set

        when:
        res = entityMapService.buildIncludesMap("Bookz")

        then:
        res.className == 'Bookz' // [className: 'Bookz', props: ['name']]
        res.fields == ['id', 'version', 'name', 'cost'] as Set

        when:
        res = entityMapService.buildIncludesMap("Bookz", ['name', 'enumThings.*'])

        then:
        res.className == 'Bookz' // [className: 'Bookz', props: ['name']]
        res.fields == ['name', 'enumThings'] as Set
        res.nestedIncludes.size() == 1
        res.nestedIncludes['enumThings'].with{
            className == 'gorm.tools.beans.domain.EnumThing'
            fields == ['id', 'testEnum', 'version', 'enumIdent'] as Set
        }

        when:
        res = entityMapService.buildIncludesMap("BookAuthor", ['*', 'book.*', 'bookAuthor.id', 'bookAuthor.age'])

        then:
        res.className == 'BookAuthor' // [className: 'Bookz', props: ['name']]
        res.fields == ['id', 'age', 'version', 'book', 'bookAuthor'] as Set
        res.nestedIncludes.size() == 2
        // res.nested == [
        //     book: ['className': 'gorm.tools.beans.domain.Bookz', props: ['id', 'version', 'name', 'cost'] as Set],
        //     bookAuthor: ['className': 'gorm.tools.beans.domain.BookAuthor', props: ['id', 'age'] as Set]
        // ]

    }

    BookAuthor makeBookAuthor(){
        def ba = new BookAuthor(
            age: 5,
            book: new Bookz(
                name: 'atlas',
                stringList: ["foo", "bar"],
                bazMap: ["testing": 99]
            ),
            bookAuthor: new BookAuthor(
                bookAuthor: new BookAuthor(
                    age: 2
                ),
                book: new Bookz(
                    name: 'shrugged',
                    cost: 4,
                    bazMap: ["test": 1],
                    stringList: ["buzz", "booz"]
                )
            )
        )
        return ba
    }

    void "test createEntityBeanMap"() {
        when: 'sanity check'
        def emap = entityMapService.createEntityMap(makeBookAuthor(), ['*', 'book.id'])

        then:
        4 == emap.size()
        ['id', 'age', 'version', 'book'].containsAll(emap.getIncludes())
    }

    void "test null assoc"() {
        when:
        def ba = new BookAuthor( age: 5)
        def result = entityMapService.createEntityMap(ba, ['age', 'book.id'])

        then:
        result == [ age: 5, book: null ]
    }

    void "test bookz"() {
        setup:
        Bookz book = new Bookz(name: 'foo', cost: 10.00, stringList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2])

        expect:
        result == entityMapService.createEntityMap(book, fields) //BeanPathTools.buildMapFromPaths(book, fields)

        where:
        fields                        | result
        ['*']                         | [id: 1, version: null, name: 'foo', cost: 10.00]
        ['name', 'company']           | [name: 'foo', company: 'Tesla']
        ['name', 'simplePogo.name']   | [name: 'foo', simplePogo: [ name: 'fly']]
        ['name', 'bookTags.name']     | [ name: 'foo', bookTags: [[ name: 'red'], [ name: 'green']] ]
        ['*', 'stringList', 'bazMap'] | [name: 'foo', cost: 10.00, id: 1, version: null, stringList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2]]
    }

    void "test buildMapFromPaths Enum"() {
        setup:
        EnumThing et = new EnumThing(
            testEnum: TestEnum.FOO,
            enumIdent: TestEnumIdent.Num2
        )

        expect:
        exp == entityMapService.createEntityMap(et, path)

        where:

        path                                | exp
        ['enumIdent.*']                     | [enumIdent:[id:2, name:'Num2']]
        ['enumIdent.id', 'enumIdent.num']   | [enumIdent:[id:2, num:'2-Num2']]
        ['testEnum', 'enumIdent']           | [testEnum:'FOO', enumIdent:[id:2, name:'Num2']]

    }

    void "test createEntityBeanMap with EnumThing list"() {
        when:
        Bookz book = new Bookz(name: 'foo', cost: 10.00)
        (1..2).each{id ->
            def et = new EnumThing(
                testEnum: TestEnum.FOO,
                enumIdent: TestEnumIdent.Num2
            )
            et.id = id
            book.addToEnumThings(et)
        }
        def result = entityMapService.createEntityMap(book, ['*', 'enumThings.*'])

        then:
        result == [
            id: 1,
            version: null,
            name: 'foo',
            cost: 10.00,
            enumThings: [
                [id: 1, testEnum: 'FOO', version:null, enumIdent: [id:2, name:'Num2']],
                [id: 2, testEnum: 'FOO', version:null, enumIdent: [id:2, name:'Num2']]
            ]
        ]

        when:
        result = entityMapService.createEntityMap(book, ['enumThings.testEnum', 'enumThings.enumIdent'])

        then:
        result == [
            enumThings: [
                [testEnum: 'FOO', enumIdent: [id:2, name:'Num2']],
                [testEnum: 'FOO', enumIdent: [id:2, name:'Num2']]
            ]
        ]
    }

    void "BookAuthor tests "() {
        setup:
        def obj = makeBookAuthor()

        expect:
        exp == entityMapService.createEntityMap(obj, path)

        where:
        path                          | exp
        ['*']                         | [id: 2, version: null, age: 5]
        ['age']                       | [age: 5]
        ['book.name']                 | [book: [name: 'atlas']]
        ['bookAuthor.bookAuthor.age'] | [bookAuthor: [bookAuthor: [age: 2]]]
        ['bookAuthor.book.cost']      | [bookAuthor: [book: [cost: 4]]]
        ['bookAuthor.book.*']         | [bookAuthor: [book: [cost: 4, name: 'shrugged', id: 1, version: null]]]
        ['bookAuthor.*']              | [bookAuthor: [id: 2, age: 0, version: null]]
    }

    void "BookAuthor tests complex"() {
        when:
        def emap = entityMapService.createEntityMap(makeBookAuthor(), ['*', 'book.stringList', 'book.bazMap'])

        then:
        emap == [
            id: 2, version: null, age: 5,
            book: [
                stringList: ["foo", "bar"],
                bazMap: ["testing": 99]
            ]
        ]

    }

    void "test createEntityMapList"() {
        when:
        TestSeedData.buildCustomers(5)
        def elist = entityMapService.createEntityMapList(Cust.list(), ['id', 'name', 'kind', 'type.id', 'type.name'])

        then:
        elist.size() == 5
        elist.totalCount == 5
        elist[0] == [
            id: 1, name: 'Name1', kind: 'COMPANY', type: [id:1, name: 'name']
        ]
    }
}
