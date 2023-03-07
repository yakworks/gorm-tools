package gorm.tools.mango.jpql

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import spock.lang.IgnoreRest
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
        SELECT new map( SUM(kitchenSink.amount) as amount_sum,kitchenSink.kind as kind,kitchenSink.ext.name as ext_name )
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
        criteria.order("sinkLink_amount_sum", 'asc')
        criteria.lt("sinkLink_amount_sum", 100.0)

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''\
        SELECT new map( SUM(kitchenSink.sinkLink.amount) as sinkLink_amount_sum,kitchenSink.kind as kind )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind
        HAVING (SUM(kitchenSink.sinkLink.amount) < :p1)
        ORDER BY sinkLink_amount_sum ASC ''')
        queryInfo.paramMap == [p1: 100.0]
    }

    void "test aggreagate without group"() {
        when: "aggregate without having"
        MangoDetachedCriteria criteria = KitchenSink.repo.query(
            projections: [amount:'sum'],
            q: [
                'amount': ['$gte': 100.0]
            ]
        )
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT new map( SUM(kitchenSink.amount) as amount_sum )
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.amount >= :p1)
        ''')
    }

    void "projections having with in"() {
        when: "having with in"
        MangoDetachedCriteria criteria = KitchenSink.repo.query(
            projections: [kind:'group', amount:'sum'],
            q:[
                kind:['CLIENT', 'VENDOR'],
                amount:['$gte':100]
            ]
        )
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT new map( kitchenSink.kind as kind,SUM(kitchenSink.amount) as amount_sum )
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.kind IN (:p1,:p2) AND kitchenSink.amount >= :p3)
            GROUP BY kitchenSink.kind
        ''')

    }

    void "projections having with is not null"() {
        setup:
        QueryArgs args = QueryArgs.of(q:[kind:'$isNotNull'], projections: [kind:'group', amount:'sum'])

        when: "having with in"
        MangoDetachedCriteria criteria = KitchenSink.repo.query(args)
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT new map( kitchenSink.kind as kind,SUM(kitchenSink.amount) as amount_sum )
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.kind IS NOT NULL )
            GROUP BY kitchenSink.kind
        ''')

    }

    void "projections having with between"() {
        when: "having with in"
        MangoDetachedCriteria criteria = KitchenSink.repo.query(
            projections: [localDate:'group', amount:'sum'],
            q:[
                localDate:['$between':[ "2023-01-01", "2023-01-07" ]],
                localDateTime:['$between':[ "2023-01-01", "2023-01-07" ]]
            ]
        )
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT new map( kitchenSink.localDate as localDate,SUM(kitchenSink.amount) as amount_sum )
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE ((kitchenSink.localDate >= :p1 AND kitchenSink.localDate <= :p2)
            AND (kitchenSink.localDateTime >= :p3 AND kitchenSink.localDateTime <= :p4))
            GROUP BY kitchenSink.localDate
        ''')
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
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE (kitchenSink.ext.id=:p1 AND kitchenSink.thing.id=:p2 AND kitchenSink.inactive=:p3)
        GROUP BY kitchenSink.kind
        HAVING (SUM(kitchenSink.sinkLink.amount) < :p4)
        ORDER BY sinkLink_amount_sum ASC''')
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
