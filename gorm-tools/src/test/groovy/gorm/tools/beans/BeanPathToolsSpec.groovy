/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans


import org.springframework.mock.web.MockHttpServletRequest

import gorm.tools.beans.domain.BookAuthor
import gorm.tools.beans.domain.Bookz
import gorm.tools.beans.domain.PropsToMapTest
import gorm.tools.testing.unit.DataRepoTest
import grails.web.servlet.mvc.GrailsParameterMap
import spock.lang.Ignore
import spock.lang.Specification

class BeanPathToolsSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains Bookz, BookAuthor
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

    void "propsToMap"() {
        setup:
        def obj = makeBookAuthor()

        expect:
        Map act = [:]
        exp == BeanPathTools.propsToMap(obj, path, act)
        exp == act

        where:
        path                        | exp
        'age'                       | [age: 5]
        'badField'                  | [:]
        'book.name'                 | [book: [name: 'atlas']]
        'bad1.foo'                  | [:]
        'bookAuthor.bookAuthor.age' | [bookAuthor: [bookAuthor: [age: 2]]]
        'bookAuthor.book.bad'       | [bookAuthor: [book: [:]]]
        'bookAuthor.book.cost'      | [bookAuthor: [book: [cost: 4]]]
        'bookAuthor.book.*'         | [bookAuthor: [book: [cost: 4, hiddenProp:null, name: 'shrugged', id: 1, version: null, bazMap: [test: 1], stringList: null]]]
    }

    @Ignore
    void "Check if nested object not in the db"() {
        setup:
        def obj = makeBookAuthor().save(flush: true)
        Bookz.repo.removeById(obj.book.id)
        Bookz.repo.flush()
        expect:
        obj.book.id != null
        Bookz.get(obj.book.id) == null
        Map act = [:]
        exp == BeanPathTools.propsToMap(obj, path, act)
        exp == act
        where:
        path                   | exp
        'value'                | [age: 7]
        'value1'               | [:]
        'book.foo'           | [book: [name: 'atlas']]
        'left1.foo'            | [:]
        'bookAuthor.bookAuthor.value'  | [bookAuthor: [bookAuthor: [age: 2]]]
        'bookAuthor.book.value1' | [bookAuthor: [book: [:]]]
        'bookAuthor.book.bar'    | [bookAuthor: [book: [cost: 4]]]
        'bookAuthor.book.*'      | [bookAuthor: [book: [cost: 4, name: 'shrugged', id: 1, version: null, bazMap: null, stringList: null]]]
        'bookAuthor.*'             | [bookAuthor: [id: 2, age: 0, version: null]]
    }

    void "test propsToMap for a non domain"() {
        setup:
        PropsToMapTest nested = new PropsToMapTest(field: "test2", field2: 2L,
            nested: new PropsToMapTest(field: 'test3'))
        Map map = [a: 'a', b: 'b', c: 'c']
        List list = [1, 2, 3]
        PropsToMapTest test = new PropsToMapTest(field: "test", field2: 1L, field3: 1L, field4: true,
            field5: false, field6: map, field7: list, nested: nested)

        expect:
        Map act = [:]
        exp == BeanPathTools.propsToMap(test, path, act)
        exp == act
        where:
        path                  | exp
        'field'               | [field: "test"]
        'field2'              | [field2: 1L]
        'field3'              | [field3: 1L]
        'field4'              | [field4: true]
        'field5'              | [field5: false]
        'field6'              | [field6: [a: 'a', b: 'b', c: 'c']]
        'field7'              | [field7: [1, 2, 3]]
        'nested.field'        | [nested: [field: "test2"]]
        'nested.nested.field' | [nested: [nested: [field: "test3"]]]
    }

    void "test propsToMap for a non domain2"() {
        setup:
        PropsToMapTest nested = new PropsToMapTest(field: "test2", field2: 2L,
            nested: new PropsToMapTest(field: "some_text"))
        Map map = [a: 'a', b: 'b', c: 'c']
        List list = [1, 2, 3]
        PropsToMapTest test = new PropsToMapTest(field: "test", field2: 1L, field3: 1L, field4: true,
            field5: false, field6: map, field7: list, field8: 50.0, field9: 101.1d, nested: nested)

        Map expectedMap = [
            field : "test",
            field2: 1L,
            field3: 1L,
            field4: true,
            field5: false,
            field6: [a: 'a', b: 'b', c: 'c'],
            field7: [1, 2, 3],
            field8: 50.0,
            field9: 101.1d,
            nested: [
                field : "test2",
                field2: 2L,
                field3: null,
                field4: null,
                field5: false,
                field6: null,
                field7: null,
                field8: null,
                field9: null,
                nested: [
                    field : "some_text", field2: 0L, field3: null, field4: null,
                    field5: false, field6: null, field7: null, field8: null, field9: null, nested: null
                ]
            ]
        ]

        when:
        Map result = [:]
        BeanPathTools.propsToMap(test, '*', result)

        then:
        result == expectedMap
    }


    void "test buildMapFromPaths entity no *"() {
        when:
        BookAuthor ba = makeBookAuthor()
        def res = BeanPathTools.buildMapFromPaths(ba, ['book'])

        then:
        res == [book:[id: 1]]

    }

    @Ignore //FIXME
    void "test buildMapFromPaths"() {
        setup:
        Bookz book = new Bookz(name: 'foo', cost: 10.00, stringList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2])

        expect:
        result == BeanPathTools.buildMapFromPaths(book, fields)

        where:
        fields             | result
        ['name','company'] | [name: 'foo', company: 'Tesla']
        ['*']              | [name: 'foo', cost: 10.00, id: 1, version: null, stringList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2]]
    }

    void "test buildMapFromPaths with transient"() {
        setup:
        Bookz object = new Bookz(name: 'foo', cost: 10.00)

        expect: 'company is transient'
        [name: 'foo', company: 'Tesla'] == BeanPathTools.buildMapFromPaths(object, ['name','company'])

    }

    void "test buildMapFromPaths for all fields using delegating bean"() {
        setup:
        Bookz object = new Bookz(name: 'foo', cost: 50.0, enumThings: null)
        List fields = ['*']

        when:
        Map result = BeanPathTools.buildMapFromPaths(object, fields, true)

        then:
        null != result
        result.name == 'foo'
        result.cost == 50.0
        result.enumThings == null
    }

    void "test flattenMap"() {
        setup:
        String json = """
        {
            param1: 'value1',
            param2: {
                param3: 'value3',
                param4: {
                    param5: 'value5'
                }

            }
        }
        """
        MockHttpServletRequest request = new MockHttpServletRequest()
        request.setContentType('application/json')
        request.setContent(json.getBytes())

        when:
        GrailsParameterMap result = BeanPathTools.flattenMap(request)

        then:
        result != null
        result.param1 == 'value1'
        result.'param2.param3' == 'value3'
        result.'param2.param4.param5' == 'value5'
    }

    @Ignore
    void "test getIncludes"(){
        expect:
        res == BeanPathTools.getIncludes("Bookz", fields)

        where:
        fields            | res
        ['name']          | ['name']
        ['*']             | ['id', 'version', 'name', 'cost']
        ['enumThings.*']  | ['enumThings.id', 'enumThings.testEnum', 'enumThings.version', 'enumThings.enumIdent']
        //FIXME make the following work
        //['baz.id']      | ['baz.id', 'baz']
    }
}
