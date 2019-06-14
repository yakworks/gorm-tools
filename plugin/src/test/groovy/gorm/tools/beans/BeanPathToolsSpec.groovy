/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import gorm.tools.testing.unit.GormToolsTest
import grails.persistence.Entity
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import testing.Org

class BeanPathToolsSpec extends Specification implements GormToolsTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains TestClazzA, TestClazzB, TestClazzC
    }

    //List<Class> getDomainClasses() { [TestClazzA, TestClazzB, TestClazzC] }

    void "Can get property value for a basic class"() {
        setup:
        def obj = new TestClazzA(
            foo: '1111',
            bar: -12.52,
            baz: null,
            bazList: ["1", "test", "foo"],
            bazMap: ["testKey": 1, "oneMore": 2]
        )
        expect:
        exp == BeanPathTools.getFieldValue(obj, path)

        where:
        exp                          | path
        '1111'                       | 'foo'
        -12.52                       | 'bar'
        null                         | 'baz'
        ["1", "test", "foo"]         | 'bazList'
        ["testKey": 1, "oneMore": 2] | 'bazMap'
    }

    void "Can get property value for a class hierarchy"() {
        setup:
        def obj = new TestClazzB(
            left: new TestClazzA(
                foo: '1',
                bazList: ["foo"]
            ),
            right: new TestClazzB(
                right: new TestClazzB(
                    value: 2
                ),
                left: new TestClazzA(
                    foo: '3',
                    bar: 4,
                    bazMap: ["test": 1]
                )
            ),
            value: 5
        )

        expect:
        exp == BeanPathTools.getFieldValue(obj, path)

        where:
        exp         | path
        5           | 'value'
        '1'         | 'left.foo'
        ["foo"]     | 'left.bazList'
        2           | 'right.right.value'
        4           | 'right.left.bar'
        ["test": 1] | 'right.left.bazMap'
        null        | 'right.left.bazList'
    }

    void "Get properties by path"() {
        setup:
        def obj = new TestClazzB(
            left: new TestClazzA(
                foo: '1'
            ),
            right: new TestClazzB(
                right: new TestClazzB(
                    value: 2
                ),
                left: new TestClazzA(
                    foo: '3',
                    bar: 4
                )
            ),
            value: 7
        )
        expect:
        Map act = [:]
        exp == BeanPathTools.propsToMap(obj, path, act)
        exp == act
        where:
        path                | exp
        'value'             | [value: 7]
        'value1'            | [:]
        'left.foo'          | [left: [foo: '1']]
        'left1.foo'         | [:]
        'right.right.value' | [right: [right: [value: 2]]]
        'right.left.value1' | [right: [left: [:]]]
        'right.left.bar'    | [right: [left: [bar: 4]]]
        'right.left.*'      | [right: [left: [bar: 4, foo: '3', id: 1, version: null, bazMap: null, bazList: null]]]
        'right.*'           | [right: [id: 2, value: 0, version: null]]
    }

    void "Check if nested object not in the db"() {
        setup:
        def obj = new TestClazzB(
            left: new TestClazzA(
                foo: '1'
            ).save(flush: true),
            right: new TestClazzB(
                right: new TestClazzB(
                    value: 2
                ),
                left: new TestClazzA(
                    foo: '3',
                    bar: 4
                )
            ),
            value: 7
        ).save()
        TestClazzA.repo.removeById(obj.left.id)
        TestClazzA.repo.flush()
        expect:
        obj.left.id != null
        TestClazzA.get(obj.left.id) == null
        Map act = [:]
        exp == BeanPathTools.propsToMap(obj, path, act)
        exp == act
        where:
        path                | exp
        'value'             | [value: 7]
        'value1'            | [:]
        'left.foo'          | [left: [foo: '1']]
        'left1.foo'         | [:]
        'right.right.value' | [right: [right: [value: 2]]]
        'right.left.value1' | [right: [left: [:]]]
        'right.left.bar'    | [right: [left: [bar: 4]]]
        'right.left.*'      | [right: [left: [bar: 4, foo: '3', id: 1, version: null, bazMap: null, bazList: null]]]
        'right.*'           | [right: [id: 2, value: 0, version: null]]
    }

    void "Property returns list of domains"() {
        setup:
        def obj = new TestClazzC(
            id: 9,
            value: 10
        )
        expect:
        Map act = [:]
        exp == BeanPathTools.propsToMap(obj, path, act)
        exp == act
        where:
        path          | exp
        'value'       | [value: 10]
        'fooValues.*' | [fooValues: [[id: 1, bar: null, foo: 'val 1', version: null, bazMap: null, bazList: null], [id: 1, bar: null, foo: 'val 2', version: null, bazMap: null, bazList: null]]]
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

    void "test buildMapFromPaths"() {
        setup:
        TestClazzA object = new TestClazzA(foo: 'foo', bar: 10.00, bazList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2])
        object.addToBaz(new TestClazzC(value: 23))

        expect:
        result == BeanPathTools.buildMapFromPaths(object, fields)

        where:
        fields          | result
        ['foo']         | [foo: 'foo']
        ['*']           | [foo: 'foo', bar: 10.00, id: 1, version: null, bazList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2]]
        ['*', 'baz.value'] | [foo: 'foo', bar: 10.00, id: 1, version: null, baz: [[value: 23]], bazList: ["1", "test", "foo"], bazMap: ["testKey": 1, "oneMore": 2]]
    }

    void "test buildMapFromPaths for all fields using delegating bean"() {
        setup:
        TestClazzA object = new TestClazzA(foo: 'foo', bar: 50.0, baz: null)
        List fields = ['*']

        when:
        Map result = BeanPathTools.buildMapFromPaths(object, fields, true)

        then:
        null != result
        result.foo == 'foo'
        result.bar == 50.0
        result.baz == null
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

    void "test getIncludes"(){
        expect:
        result == BeanPathTools.getIncludes("TestClazzA", fields)

        where:
        fields          | result
        ['foo']         | ['foo']
        ['*']           | ['id', 'foo', 'version', 'bar']
        ['baz.*']       | ['baz.id', 'baz.value', 'baz.version', 'baz']
        //FIXME make the following work
        //['baz.id']      | ['baz.id', 'baz']
    }

}

@Entity
class TestClazzA {
    Long id
    String foo
    BigDecimal bar

    TestClazzA(){
        id = 1
    }

    //See https://sysgears.com/articles/advanced-gorm-features-inheritance-embedded-data-maps-and-lists-storing/
    List<String> bazList

    Map bazMap

    static hasMany = [baz: TestClazzC, bazList: String]

    static mapping = {
        id generator:'assigned'
    }
}

@Entity
class TestClazzB {
    TestClazzB(){
        id = 2
    }
    TestClazzA left
    TestClazzB right
    int value

    static mapping = {
        id generator:'assigned'
    }
}

@Entity
class TestClazzC {
    TestClazzC(){
        id = 3
    }
    int value

    static mapping = {
        id generator:'assigned'
    }

    List getFooValues() {
        [
            new TestClazzA(foo: 'val 1'),
            new TestClazzA(foo: 'val 2')
        ]
    }

}

class PropsToMapTest {
    String field
    long field2
    Long field3
    Boolean field4
    boolean field5
    Map field6
    List field7
    BigDecimal field8
    Double field9
    PropsToMapTest nested
}
