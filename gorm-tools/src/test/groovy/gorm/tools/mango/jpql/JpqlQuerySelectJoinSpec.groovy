package gorm.tools.mango.jpql

import javax.persistence.criteria.JoinType

import gorm.tools.mango.MangoBuilder
import gorm.tools.mango.MangoDetachedCriteria
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip

/**
 * Test for property and selects. Also tests joins
 */
class JpqlQuerySelectJoinSpec extends Specification implements GormHibernateTest  {

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

    void "Test select using select in query args"() {
        when:"🎯props are set on the criteria"
        def criteria = KitchenSink.query([
            select: ['id', 'name'],
            sort: 'thing.name'
        ]).join("thing", JoinType.LEFT)

        List list = criteria.list()

        def builder = JpqlQueryBuilder.of(criteria)
        JpqlQueryInfo queryInfo = builder.buildSelect()

        then:
        list.size() == 10

        compareQuery(queryInfo.query, """
            SELECT kitchenSink.id as id, kitchenSink.name as name
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            LEFT JOIN kitchenSink.thing
            ORDER BY kitchenSink.thing.name ASC
        """)

        when:
        List<Map> jpqlList = doList(queryInfo)
        then:
        jpqlList.size() == 10

    }

    void "Test select with restrictions"() {
        when:"🎯props are set on the criteria"
        def criteria = KitchenSink.query([
            select: ['id', 'name', 'ext.name'],
            q:[
                id: [
                    '$in': [5, 9]
                ],
                'amount.$gt': 2
            ],
            sort: 'thing.name'
        ]).join("thing", JoinType.LEFT)

        List list = criteria.list()

        def builder = JpqlQueryBuilder.of(criteria)
        JpqlQueryInfo queryInfo = builder.buildSelect()

        then:
        list.size() == 2

        compareQuery(queryInfo.query, """
            SELECT kitchenSink.id as id, kitchenSink.name as name, kitchenSink.ext.name as ext_name
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            LEFT JOIN kitchenSink.thing
            WHERE kitchenSink.id IN ( :p1,:p2 ) AND kitchenSink.amount > :p3
            ORDER BY kitchenSink.thing.name ASC
        """)

        when:
        List<Map> jpqlList = doList(queryInfo)
        then:
        jpqlList.size() == 2

    }

    @Ignore
    void "Test distinct on property"() {
        given:"Some criteria"
        def criteria = KitchenSink.query(
            'name': 'Bob'
        ).distinct("ext.kitchenParent.thing.name")

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        //builder.hibernateCompatible = true
        def query = builder.buildSelect().query

        then:"The query is valid"
        query != null
        compareQuery(queryInfo.query,
            'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1)'
        )
    }

    List doList(JpqlQueryInfo queryInfo, Map args = [:]){
        def staticApi = KitchenSink.repo.gormStaticApi()
        def spq = new PagedQuery(staticApi)
        def list = spq.list(queryInfo.query, queryInfo.paramMap, args)
        return list

    }

}
