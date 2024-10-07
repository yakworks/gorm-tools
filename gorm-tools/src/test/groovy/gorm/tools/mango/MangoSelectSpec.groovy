package gorm.tools.mango

import javax.persistence.criteria.JoinType

import gorm.tools.mango.hibernate.PathKeyMapPagedList
import gorm.tools.mango.jpql.JpqlQueryBuilder
import gorm.tools.mango.jpql.JpqlQueryInfo
import gorm.tools.mango.jpql.PagedQuery
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.commons.model.SimplePagedList
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

    void "Test property and join - 1 level down"() {
        when:"🎯props are set on the criteria"
        def criteria = KitchenSink.query(null)
            .property("id")
            .property("thing.name")
            // FAILS if nested to deep, need to use mapList which uses the JPQL
            //.property("ext.thing.name")
        //uses the hibernate query list
        List list = criteria.list()
        List listJpql = criteria.mapList()

        then:
        //since join is not specified it only shows 9 rows since one thing is null, should show 10 if we want
        // all the kitchen sinks
        list.size() == 9
        listJpql.size() == 9

        Map row1 = list[0] as Map
        row1.keySet() == ['id', 'thing_name'] as Set

        //jpql uses the pathKeyMap so [thing_name:..] will be [thing:[name:..]
        Map row1Jpql = listJpql[0] as Map
        row1Jpql.keySet() == ['id', 'thing'] as Set
        row1Jpql['thing'].keySet() == ['name'] as Set

        when: "🎯add left join to thing"
        criteria = KitchenSink.query(null)
            .property("id").property("thing.name")
            .join("thing", JoinType.LEFT)

        list = criteria.list()
        listJpql = criteria.mapList()

        then: "will return the 10 rows"
        list.size() == 10
        listJpql.size() == 10

        when: "🎯sort does not add join and needs it too"
        criteria = KitchenSink.query(null)
                .property("id")
                .order("thing.name")

        list = criteria.list()
        listJpql = criteria.mapList()

        then: "will only return the 9 rows without specifying join"
        list.size() == 9
        listJpql.size() == 9


        when: "🎯adding in the join will fix it"
        criteria = KitchenSink.query(null)
                .property("id")
                .order("thing.name")
                .join("thing", JoinType.LEFT)

        list = criteria.list()
        listJpql = criteria.mapList()

        then: "will return all 10 rows now"
        list.size() == 10
        listJpql.size() == 10

    }

    void "Test property - 2 levels deep"() {
        when:"🎯props are set on the criteria"
        def criteria = KitchenSink.query(null)
            .property("id")
            .property("ext.thing.name")

        // FAILS if nested to deep, need to use the mapList which uses the JPQL
        // List list = criteria.list()

        List listJpql = criteria.mapList()

        then:
        listJpql.size() == 10
        listJpql instanceof PathKeyMapPagedList
        //jpql uses the pathKeyMap so [thing_name:..] will be [thing:[name:..]
        Map row1Jpql = listJpql[0] as Map
        row1Jpql.keySet() == ['id', 'ext'] as Set

        row1Jpql == [
            id:1,
            ext:[
                thing:[
                    name: 'Thing1'
                ]
            ]
        ]

    }

    void "Test property - aliasToMap"() {
        when:"🎯props are set on the criteria"
        def criteria = KitchenSink.query(null)
            .property("id")
            .property("ext.thing.name")

        List listJpql = criteria.mapList(aliasToMap: true)

        then:
        listJpql.size() == 10
        listJpql instanceof PathKeyMapPagedList
        //jpql uses the pathKeyMap so [thing_name:..] will be [thing:[name:..]
        Map row1Jpql = listJpql[0] as Map
        row1Jpql.keySet() == ['id', 'ext_thing_name'] as Set

        row1Jpql == [
            id:1,
            ext_thing_name: 'Thing1'
        ]

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
