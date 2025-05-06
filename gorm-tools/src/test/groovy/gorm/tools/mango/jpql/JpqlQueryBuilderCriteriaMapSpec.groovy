package gorm.tools.mango.jpql

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.config.GormConfig
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip

/**
 * Test for JPA builder with closures not map builder
 */
class JpqlQueryBuilderCriteriaMapSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [KitchenSink, SinkItem]

    @Autowired
    GormConfig gormConfig

    boolean compareQuery(String hql, String expected){
        assert formatAndStrip(hql) == formatAndStrip(expected)
        return true
    }

    void setupSpec(){
        KitchenSink.withTransaction {
            KitchenSink.repo.createKitchenSinks(10)
            assert KitchenSink.get(1)
            def list = KitchenSink.list()
            assert list
            assert KitchenSink.findWhere(num: '1')
            flushAndClear()
        }
    }

    void "Test build simple select with added where"() {
        when:

        def criteria = KitchenSink.query(
            name: 'Blue Cheese'
        )
        def crit = criteria.where([num: '1'])

        def queryInfo = JpqlQueryBuilder.of(crit).buildSelect()

        then:"The query is valid"
        compareQuery(queryInfo.query, """
            SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE kitchenSink.name=:p1 AND kitchenSink.num=:p2
        """)

        when:
        //normal
        List critList = crit.list()
        List mapList = crit.mapList()

        then:
        critList.size() == 1
        critList[0] == KitchenSink.get(1)

        mapList.size() == 1
        critList[0] == KitchenSink.get(1)
    }

    void "Test build select with or"() {
        given:"Some criteria"

        def criteria = KitchenSink.query(
            '$or': [
                ['num': '1'],
                ['num': '2']
            ]
        )
        //XXX @SUD lets get error checking in place for this, where they pass in duplicate key
        // def criteria = KitchenSink.query(
        //     '$or': [
        //         'name': 'Bob',
        //         'name': 'Fred'
        //     ]
        // )

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        final queryInfo = builder.buildSelect()

        then:"The query is valid"
        queryInfo.query!= null
        //NOTE TODO, see the same query using closure, this adds extra parens
        compareQuery(queryInfo.query, """
        SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE (kitchenSink.num=:p1 OR kitchenSink.num=:p2)
        """)
        queryInfo.parameters == ['1', '2']

        criteria.list().size() == 2
    }

    void "Test build select with or and ilike"() {
        given:"Some criteria"
        def criteria = KitchenSink.query(
            '$or': [
                ['name2': 'org1%'],
                ['num': '1%']
            ]
        )

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        builder.enableDialectFunctions(true)
        final queryInfo = builder.buildSelect()

        then:"The query is valid"
        queryInfo.query!= null
        //NOTE TODO, see the same query using closure, this adds extra parens
        compareQuery(queryInfo.query, """
        SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE ( flike(kitchenSink.name2, :p1 ) = true OR flike(kitchenSink.num, :p2 ) = true )
        """)
        queryInfo.parameters == ['org1%', '1%']

        when:
        List<Map> list = criteria.mapList()

        then:
        list.size() == 2
    }

    @Ignore //blowing up on unknown field, which is should bu we need a way to override
    void "Test eqf"() {
        given:
        def criteria = KitchenSink.query(
            num: 123, name: 'blue',
            name2:['$eqf': 'bar.baz.name2']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).entityAlias("foo")
        builder.projectionAliases['bar.baz.name2'] = 'eq'
        final queryInfo = builder.buildSelect()

        then: "The query is valid"
        queryInfo.query == strip("""
            SELECT DISTINCT foo FROM yakworks.testing.gorm.model.KitchenSink AS foo
            WHERE (foo.num=:p1 AND foo.name=:p2)
        """)
        queryInfo.where == "(foo.num=:p1 AND foo.name=:p2)"
        queryInfo.parameters == ['123', 'blue']
    }

}
