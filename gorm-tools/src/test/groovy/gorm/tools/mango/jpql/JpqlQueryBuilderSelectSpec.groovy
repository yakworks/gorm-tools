package gorm.tools.mango.jpql

import gorm.tools.mango.MangoDetachedCriteria
import spock.lang.PendingFeature
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * Test for JPA builder
 */
class JpqlQueryBuilderSelectSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [KitchenSink]

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
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1)'
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
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1)'
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
        queryInfo.query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE ((kitchenSink.name=:p1 OR kitchenSink.name=:p2))'
        queryInfo.parameters == ['Bob', 'Fred']

    }

    void "Test projections simple"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            sum('amount')
            groupBy('kind')
            groupBy("ext.name")
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT new map( SUM(kitchenSink.amount) as amount,kitchenSink.kind as kind,kitchenSink.ext.name as ext_name )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind,kitchenSink.ext.name
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
        criteria.order("sinkLink_amount")
        criteria.lt("sinkLink_amount", 100.0)

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''\
        SELECT new map( SUM(kitchenSink.sinkLink.amount) as sinkLink_amount,kitchenSink.kind as kind )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind
        HAVING (SUM(kitchenSink.sinkLink.amount) < :p1)
        ORDER BY sinkLink_amount ASC ''')
        queryInfo.paramMap == [p1: 100.0]
    }

    void "projections having with alias"() {
        given:
        def criteria = KitchenSink.query("createdByJobId":1)
            .groupBy('sinkLink.createdByJobId as createdByJobId')
            .groupBy("kind")


        when:
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query.trim() == strip("""
        SELECT new map( kitchenSink.sinkLink.createdByJobId as createdByJobId,kitchenSink.kind as kind )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.sinkLink.createdByJobId,kitchenSink.kind
        HAVING (kitchenSink.sinkLink.createdByJobId=:p1)
        """)
        queryInfo.paramMap == [p1: 1]
    }

    @PendingFeature
    void "criteria restriction projections projection property with alias"() {
        given:
        def criteria = KitchenSink.query("sinkLink.createdByJobId":1)
            .groupBy('sinkLink.createdByJobId as createdByJobId')
            .groupBy("kind")


        when:
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query.trim() == strip("""
        SELECT new map( kitchenSink.sinkLink.createdByJobId as createdByJobId,kitchenSink.kind as kind )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.sinkLink.createdByJobId,kitchenSink.kind
        HAVING (kitchenSink.sinkLink.createdByJobId=:p1)
        """)
        queryInfo.paramMap == [p1: 1]
    }

    @PendingFeature
    //verify, that when we have a restriction on an alias - eg link.createdByJobId instead of sinkLink.createdByJobId
    void "criteria restriction on alias"() {
        given:
        MangoDetachedCriteria criteria = KitchenSink.query("link.createdByJobId":1)
        criteria.createAlias("sinkLink", "link")

        when:
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query.trim() == strip("""
            SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.sinkLink.createdByJobId=:p1)
        """)
        queryInfo.paramMap == [p1: 1]
    }


    void "projections having criteria map"() {
        given: "Some criteria"

        def criteria = KitchenSink.query (
            projections: ['sinkLink.amount':'sum', 'kind':'group'],
            q: [
                'ext.id': 1,
                'thingId': 2,
                inactive: true,
                'sinkLink_amount.$lt':100.0
            ],
            sort:[sinkLink_amount:'asc']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''\
        SELECT new map( SUM(kitchenSink.sinkLink.amount) as sinkLink_amount,kitchenSink.kind as kind )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE (kitchenSink.ext.id=:p1 AND kitchenSink.thing.id=:p2 AND kitchenSink.inactive=:p3)
        GROUP BY kitchenSink.kind
        HAVING (SUM(kitchenSink.sinkLink.amount) < :p4)
        ORDER BY sinkLink_amount ASC''')
        queryInfo.paramMap == [p1: 1, p2: 2, p3: true, p4: 100]
    }

    void "Test projections simple with aliases"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            sum('amount as x')
            groupBy('kind as y')
            groupBy("ext.name as name")
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT new map( SUM(kitchenSink.amount) as x,kitchenSink.kind as y,kitchenSink.ext.name as name )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind,kitchenSink.ext.name
        """)
        //
        // when:
        // List res = KitchenSink.executeQuery(query)
        //
        // then:
        // res.size() == 2
    }

    void "Test projections with others"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            max('amount as maxam')
            min('amount as minam')
            avg('amount as avgam')
            groupBy('kind as y')
            groupBy("ext.name as name")
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT new map( MAX(kitchenSink.amount) as maxam,MIN(kitchenSink.amount) as minam,AVG(kitchenSink.amount) as avgam,kitchenSink.kind as y,kitchenSink.ext.name as name )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind,kitchenSink.ext.name
        """)
        //
        // when:
        // List res = KitchenSink.executeQuery(query)
        //
        // then:
        // res.size() == 2
    }

}
