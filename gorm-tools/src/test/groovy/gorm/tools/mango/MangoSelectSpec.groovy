package gorm.tools.mango

import javax.persistence.criteria.JoinType

import gorm.tools.mango.jpql.JpqlQueryBuilder
import gorm.tools.mango.jpql.JpqlQueryInfo
import gorm.tools.mango.jpql.PagedQuery
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip

/**
 * Tests the select list
 */
class MangoSelectSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [KitchenSink, SinkItem]

    boolean compareQuery(String hql, String expected){
        assert formatAndStrip(hql) == formatAndStrip(expected)
        return true
    }

    void setupSpec(){
        KitchenSink.withTransaction {
            KitchenSink.repo.createKitchenSinks(10)
            def ks1 = KitchenSink.get(1)
            def list = KitchenSink.list()
            assert list
            assert KitchenSink.findWhere(num: '1')
            //set one to have null thing so that when we sort etc we can test the join
            ks1.thing = null
            ks1.persist()
            flushAndClear()
            assert KitchenSink.get(1).thing == null
        }

    }

    // this also works
    // def criteria = KitchenSink.query{
    //     property("id")
    //     property("thing.name")
    // }.join("thing", JoinType.LEFT)

    //this also works and produces same thing if we do
    //def criteria = KitchenSink.query(null).distinct("id").distinct("name")

    // this does not work for some reason
    // def criteria = KitchenSink.query{
    //     property("id")
    //     property("name")
    // }

    void "Test select using property method"() {
        when:"🎯props are set on the criteria"
            def criteria = KitchenSink.query(null)
                .property("id").property("thing.name")

            List list = criteria.list()

        then:
            //since join is not specified it only shows 9 rows since one thing is null, should show 10 if we want
            // all the kitchen sinks
            list.size() == 9
            Map row1 = list[0] as Map
            row1.keySet() == ['id', 'thing_name'] as Set


        when: "🎯add left join to thing"
            list = KitchenSink.query(null)
                .property("id").property("thing.name")
                .join("thing", JoinType.LEFT)
                .list()

        then: "will return the 10 rows"
            list.size() == 10


        when: "🎯sort does not add join and needs it too"
            list = KitchenSink.query(null)
                .property("id")
                .order("thing.name")
                .list()

        then: "will only return the 9 rows without specifying join"
            list.size() == 9


        when: "🎯adding in the join will fix it"
            list = KitchenSink.query(null)
                .property("id")
                .order("thing.name")
                .join("thing", JoinType.LEFT)
                .list()

        then: "will return all 10 rows now"
            list.size() == 10

    }

    void "test select single column"() {
        when: "selecting single property"
        def list = KitchenSink.query([
            select: ['id']
        ]).list()

        then: "Its not in a map, its a list of primitives"
        list.size() == 10
        list[0] instanceof Long
        list[0] == 1
    }

    void "Test select using select in query args"() {
        when:"🎯props are set on the criteria"
        def criteria = KitchenSink.query([
            select: ['id', 'name'],
            sort: 'thing.name'
        ])
        List list = criteria.list()

        then:
        //since join is not specified it only shows 9 rows since one thing is null,
        // should show 10 if we want all the kitchen sinks
        list.size() == 9
        Map row1 = list[0] as Map
        row1.keySet() == ['id', 'name'] as Set


        when: "🎯add left join to thing"
        list = KitchenSink.query([
            select: ['id', 'name'],
            sort: 'thing.name'
        ])
            .join("thing", JoinType.LEFT)
            .list()

        then: "will return the 10 rows"
        list.size() == 10

    }

}
