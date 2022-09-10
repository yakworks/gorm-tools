package gorm.tools.mango

import gorm.tools.mango.jpql.JpqlQueryBuilder
import yakworks.gorm.testing.hibernate.GormToolsHibernateSpec
import grails.testing.spring.AutowiredTest
import yakworks.gorm.testing.model.KitchenSink

/**
 * Test for JPA builder
 */
class JpqlQueryBuilderSelectSpec extends GormToolsHibernateSpec implements AutowiredTest{

    List<Class> getDomainClasses() { [KitchenSink] }

    String strip(String val){
        val.stripIndent().replace('\n',' ').trim()
    }

    void "Test build simple select hibernate compatible"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'name', 'Bob'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        builder.hibernateCompatible = true
        def query = builder.buildSelect().query

        then:"The query is valid"
        query != null
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1)'
    }

    void "Test build simple select"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'name', 'Bob'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        def query = builder.buildSelect().query

        then:"The query is valid"
        query != null
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1)'
    }

    void "Test build select with or"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            or {
                eq 'name', 'Bob'
                eq 'name', 'Fred'
            }
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        final queryInfo = builder.buildSelect()

        then:"The query is valid"
        queryInfo.query!= null
        queryInfo.query == 'SELECT DISTINCT kitchenSink FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink WHERE ((kitchenSink.name=:p1 OR kitchenSink.name=:p2))'
        queryInfo.parameters == ['Bob', 'Fred']

    }

    void "Test projections simple"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            sum('amount')
            groupBy('kind')
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT new map( SUM(kitchenSink.amount) as amount_sum,kitchenSink.kind as kind )
        FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind
        """)
        //
        // when:
        // List res = KitchenSink.executeQuery(query)
        //
        // then:
        // res.size() == 2
    }

    void "projections having"() {
        given: "Some criteria"

        def criteria = KitchenSink.query {
            sum('sinkLink.amount')
            groupBy('kind')
        }
        criteria.order("sinkLink_amount_sum")
        criteria.lt("sinkLink_amount_sum", 100.0)

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''\
        SELECT new map( SUM(kitchenSink.sinkLink.amount) as sinkLink_amount_sum,kitchenSink.kind as kind )
        FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind
        HAVING (SUM(kitchenSink.sinkLink.amount) < :p1)
        ORDER BY sinkLink_amount_sum ASC ''')
        queryInfo.paramMap == [p1: 100.0]
    }

    void "projections having criteria map"() {
        given: "Some criteria"

        def criteria = KitchenSink.query (
            projections: ['sinkLink.amount':'sum', 'kind':'group'],
            q: [
                'ext.id': 1,
                'thingId': 2,
                inactive: true,
                'sinkLink_amount_sum.$lt':100.0
            ],
            sort:[sinkLink_amount_sum:'asc']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''\
        SELECT new map( SUM(kitchenSink.sinkLink.amount) as sinkLink_amount_sum,kitchenSink.kind as kind )
        FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink
        WHERE (kitchenSink.ext.id=:p1 AND kitchenSink.thing.id=:p2 AND kitchenSink.inactive=:p3)
        GROUP BY kitchenSink.kind
        HAVING (SUM(kitchenSink.sinkLink.amount) < :p4)
        ORDER BY sinkLink_amount_sum ASC''')
        queryInfo.paramMap == [p1: 1, p2: 2, p3: true, p4: 100]
    }

}
