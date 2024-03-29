package gorm.tools.mango.jpql


import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * Test for JPA builder with closures not map builder
 */
class JpqlQueryBuilderCriteriaMapSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [KitchenSink, SinkItem]

    String strip(String val){
        val.stripIndent().replace('\n',' ').trim()
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

        def query = JpqlQueryBuilder.of(crit).buildSelect().query

        then:"The query is valid"
        query == strip("""
            SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.name=:p1 AND kitchenSink.num=:p2)
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
        queryInfo.query == strip("""
        SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE (((kitchenSink.num=:p1) OR (kitchenSink.num=:p2)))
        """)
        queryInfo.parameters == ['1', '2']

        criteria.list().size() == 2
    }

}
