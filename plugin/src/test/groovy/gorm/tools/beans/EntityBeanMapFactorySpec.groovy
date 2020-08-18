/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans


import gorm.tools.beans.domain.BookAuthor
import gorm.tools.beans.domain.Bookz
import gorm.tools.beans.domain.EnumThing
import gorm.tools.beans.domain.TestEnum
import gorm.tools.beans.domain.TestEnumIdent
import gorm.tools.testing.unit.GormToolsTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class EntityBeanMapFactorySpec extends Specification implements GormToolsTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains Bookz, BookAuthor, EnumThing
    }

    void "test getIncludesForBeanMap"(){
        when:
        def res = EntityBeanMapFactory.getIncludesForBeanMap("Bookz", ['name'])

        then:
        res.className == 'Bookz' // [className: 'Bookz', props: ['name']]
        res.props == ['name'] as Set

        when:
        res = EntityBeanMapFactory.getIncludesForBeanMap("Bookz", ['*'])

        then:
        res.className == 'Bookz' // [className: 'Bookz', props: ['name']]
        res.props == ['id', 'version', 'name', 'cost'] as Set

        when:
        res = EntityBeanMapFactory.getIncludesForBeanMap("Bookz", ['name', 'enumThings.*'])

        then:
        res.className == 'Bookz' // [className: 'Bookz', props: ['name']]
        res.props == ['name', 'enumThings'] as Set
        res.nested == [enumThings: ['className': 'gorm.tools.beans.domain.EnumThing', props: ['id', 'testEnum', 'version', 'enumIdent'] as Set]]

        when:
        res = EntityBeanMapFactory.getIncludesForBeanMap("BookAuthor", ['*', 'book.*', 'bookAuthor.id', 'bookAuthor.age'])

        then:
        res.className == 'BookAuthor' // [className: 'Bookz', props: ['name']]
        res.props == ['id', 'age', 'version', 'book', 'bookAuthor'] as Set
        res.nested == [
            book: ['className': 'gorm.tools.beans.domain.Bookz', props: ['id', 'version', 'name', 'cost'] as Set],
            bookAuthor: ['className': 'gorm.tools.beans.domain.BookAuthor', props: ['id', 'age'] as Set]
        ]

        // fields            | result
        // ['name']          | [className: 'Bookz', props: ['name']]
        // ['*']             | [className: 'Bookz', props: ['id', 'version', 'name', 'cost']]
        // ['name', 'enumThings.*']  | [className: 'Bookz', props: ['name', 'enumThings'], nested:[className: 'EnumThing', props: ['id', 'testEnum', 'version', 'enumIdent']]]
        // //FIXME make the following work
        // //['baz.id']      | ['baz.id', 'baz']
    }

    BookAuthor makeBookAuthor(){
        return new BookAuthor(
            age: 5,
            book: new Bookz(
                name: 'atlas',
                stringList: ["foo"],
            ),
            bookAuthor: new BookAuthor(
                bookAuthor: new BookAuthor(
                    age: 2
                ),
                book: new Bookz(
                    name: 'shrugged',
                    cost: 4,
                    bazMap: ["test": 1]
                )
            )
        )
    }

    void "test createEntityBeanMap"() {
        when: 'sanity check'
        def emap = EntityBeanMapFactory.createEntityBeanMap(makeBookAuthor(), ['*', 'book.id'])

        then:
        4 == emap.size()
        ['id', 'age', 'version', 'book'].containsAll(emap.getIncludes())
    }

    void "test bookz"() {
        setup:
        Bookz book = new Bookz(name: 'foo', cost: 10.00, stringList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2])

        expect:
        result == EntityBeanMapFactory.createEntityBeanMap(book, fields) //BeanPathTools.buildMapFromPaths(book, fields)

        where:
        fields                        | result
        ['*']                         | [id: 1, version: null, name: 'foo', cost: 10.00]
        ['name', 'company']           | [name: 'foo', company: 'Tesla']
        ['*', 'stringList', 'bazMap'] | [name: 'foo', cost: 10.00, id: 1, version: null, stringList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2]]
    }

    void "test buildMapFromPaths Enum"() {
        when:
        EnumThing et = new EnumThing(
            testEnum: TestEnum.FOO,
            enumIdent: TestEnumIdent.Num2
        )
        def res = EntityBeanMapFactory.createEntityBeanMap(et, ['testEnum', 'enumIdent'] )

        then:
        res == [testEnum:'FOO', enumIdent:'Num2']
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
        def result = EntityBeanMapFactory.createEntityBeanMap(book, ['*', 'enumThings.*'])

        then:
        result == [
            id: 1,
            version: null,
            name: 'foo',
            cost: 10.00,
            enumThings: [
                [id: 1, testEnum: 'FOO', version:null, enumIdent: 'Num2'],
                [id: 2, testEnum: 'FOO', version:null, enumIdent: 'Num2']
            ]
        ]

        when:
        result = EntityBeanMapFactory.createEntityBeanMap(book, ['enumThings.testEnum', 'enumThings.enumIdent'])

        then:
        result == [
            enumThings: [
                [testEnum: 'FOO', enumIdent: 'Num2'],
                [testEnum: 'FOO', enumIdent: 'Num2']
            ]
        ]

    }
}
