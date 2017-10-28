package gorm.tools.beans

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.hibernate.HibernateTestMixin
import org.grails.core.DefaultGrailsDomainClass
import spock.lang.Specification
import grails.test.mixin.gorm.Domain
import grails.gorm.annotation.Entity
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.mock.web.MockHttpServletRequest
import grails.plugin.dao.GormDaoSupport

@Domain([TestClazzA, TestClazzB, TestClazzC])
@TestMixin(HibernateTestMixin)
class BeanPathToolsSpec extends Specification {

    void "Can get property value for a basic class"() {
        setup:
        def obj = new TestClazzA(
                foo: '1111',
                bar: -12.52,
                baz: null
        )
        expect:
        exp == BeanPathTools.getNestedValue(obj, path)
        where:
        exp         | path
        '1111'      | 'foo'
        -12.52      | 'bar'
        null        | 'baz'
    }

    void "Can get property value for a class hierarchy"() {
        setup:
        def obj = new TestClazzB(
                left: new TestClazzA(
                        foo: '1'
                ),
                right: new TestClazzB(
                        left: new TestClazzB(
                                value: 2
                        ),
                        right: new TestClazzA(
                                foo: '3',
                                bar: 4
                        )
                ),
                value: 5
        )

        expect:
        exp == BeanPathTools.getNestedValue(obj, path)

        where:
        exp         | path
        5           | 'value'
        '1'         | 'left.foo'
        2           | 'right.left.value'
        4           | 'right.right.bar'
    }

    void "Get properties by path"() {
        setup:
        def obj = new TestClazzB(
                left: new TestClazzA(
                        foo: '1'
                ),
                right: new TestClazzB(
                        left: new TestClazzB(
                                value: 2
                        ),
                        right: new TestClazzA(
                                foo: '3',
                                bar: 4,
                                id: 5
                        ),
                        id: 6
                ),
                value: 7,
                id: 8
        )
        expect:
        Map act = [:]
        exp == BeanPathTools.propsToMap(obj, path, act)
        exp == act
        where:
        path                    | exp
        'value'                 | [value: 7]
        'value1'                | [:]
        'left.foo'              | [left: [foo: '1']]
        'left1.foo'             | [:]
        'right.left.value'      | [right: [left: [value: 2]]]
        'right.left.value1'     | [right: [left: [:]]]
        'right.right.bar'       | [right: [right: [bar: 4]]]
        'right.right.*'         | [right: [right: [bar: 4, foo: '3', id: 5, baz:null]]]
        'right.*'               | [right: [id: 6, value: 0]]
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
        path                    | exp
        'value'                 | [value: 10]
        'fooValues.*'           | [fooValues: [[id: 1, bar: null, foo: 'val 1', baz:null], [id: 2, bar: null, foo: 'val 2', baz: null]]]
    }

    void "test buildMapFromPaths"() {
        setup:
        TestClazzA object = new TestClazzA(id: 0L, foo: 'foo', bar: 10.00, baz: null)

        expect:
        result == BeanPathTools.buildMapFromPaths(object, fields)

        where:
        fields   | result
        ['foo']  | [foo: 'foo']
        ['*']    | [foo: 'foo', bar: 10.00, baz: null, id: 0L]
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

}

@Entity
class TestClazzA {
    Long id
    Long version

    String foo
    BigDecimal bar
    List<String> baz

    def getDao() {
        new GormDaoSupport()
    }

    def getDomainClass() {
        new DefaultGrailsDomainClass(TestClazzA)
    }
}

@Entity
class TestClazzB {
    Long id
    Long version

    def left
    def right
    int value

    def getDomainClass() {
        new DefaultGrailsDomainClass(TestClazzB)
    }
}

@Entity
class TestClazzC {
    Long id
    Long version

    int value

    List getFooValues() {
        [
                new TestClazzA(id: 1, version: 0, foo: 'val 1'),
                new TestClazzA(id: 2, version: 0, foo: 'val 2')
        ]
    }

    def getDomainClass() {
        new DefaultGrailsDomainClass(TestClazzB)
    }
}
