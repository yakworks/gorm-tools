package gorm.tools.mango.jpql

import gorm.tools.mango.MangoBuilder
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
class JpqlQueryBuilderProjectionsMapSpec extends Specification implements GormHibernateTest  {

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
        crit.list().size() == 1
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

    void "Test projections simple aliasToMap"() {
        given:"Some criteria"

        def criteria = KitchenSink.query(
            projections: [
                amount:'sum',
                kind:'group',
                'thing.country':'group',
            ]
        )

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)//.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT SUM(kitchenSink.amount) as amount_sum, kitchenSink.kind as kind, kitchenSink.thing.country as thing_country
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind,kitchenSink.thing.country
        """)

        List<Map> list = criteria.mapList()
        //[[thing_country:US, amount:30.00, kind:CLIENT], [thing:US, amount:1.25, kind:PARENT], [thing:US, amount:25.00, kind:VENDOR]]
        list.size() == 3
        Map row1 = list[0]
        row1.containsKey('thing_country')
        row1.containsKey('amount')
        row1.containsKey('kind')
        row1.thing_country == 'US'
        row1.amount == 30.00
        row1.kind == KitchenSink.Kind.CLIENT
    }

    void "sum on association with sort on aggregate"() {
        given:

        def criteria = KitchenSink.query(
            projections: [
                'ext.totalDue':'sum',
                kind:'group'
            ],
            sort:[ext_totalDue_sum:'desc']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) // .aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        queryInfo.query.trim() == strip('''
            SELECT SUM(kitchenSink.ext.totalDue) as ext_totalDue_sum, kitchenSink.kind as kind
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            GROUP BY kitchenSink.kind
            ORDER BY ext_totalDue_sum DESC
        ''')
        //queryInfo.paramMap == [p1: 100.0]

        List<Map> list = criteria.mapList()
        //[[thing_country:US, amount:30.00, kind:CLIENT], [thing:US, amount:1.25, kind:PARENT], [thing:US, amount:25.00, kind:VENDOR]]
        list.size() == 3
        list[0].ext_totalDue  > list[1].ext_totalDue
        list[1].ext_totalDue  > list[2].ext_totalDue
    }

    void "sum on association with q and sort on aggregate field"() {
        given:

        def criteria = KitchenSink.query(
            projections: [
                'ext.totalDue':'sum',
                'kind':'group'
            ],
            q: [
                'ext_totalDue_sum': ['$gt': 100.0]
            ],
            sort:[ext_totalDue_sum:'desc']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) // .aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        queryInfo.query.trim() == strip('''
            SELECT SUM(kitchenSink.ext.totalDue) as ext_totalDue_sum, kitchenSink.kind as kind
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            GROUP BY kitchenSink.kind
            HAVING (SUM(kitchenSink.ext.totalDue) > :p1)
            ORDER BY ext_totalDue_sum DESC
        ''')
        queryInfo.paramMap == [p1: 100.0]

        List<Map> list = criteria.mapList()
        list.size() == 2
        list[0].ext_totalDue  > list[1].ext_totalDue
    }

    void "test aggreagate without group"() {
        when: "aggregate without having"
        MangoDetachedCriteria criteria = KitchenSink.repo.query(
            projections: [amount:'sum'],
            q: [
                'amount': ['$gte': 10.0]
            ]
        )
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT SUM(kitchenSink.amount) as amount_sum
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.amount >= :p1)
        ''')

        List<Map> list = criteria.mapList()
        list.size() == 1
        list[0].amount == 21.25
    }

    void "projections having with in"() {
        when: "having with in"
        MangoDetachedCriteria criteria = KitchenSink.repo.query(
            projections: [kind:'group', amount:'sum'],
            q:[
                kind:['CLIENT', 'VENDOR'],
                amount:['$gte':10]
            ]
        )
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT kitchenSink.kind as kind, SUM(kitchenSink.amount) as amount_sum
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.kind IN (:p1,:p2) AND kitchenSink.amount >= :p3)
            GROUP BY kitchenSink.kind
        ''')

        List<Map> list = criteria.mapList()
        list.size() == 2
        def item = list.find{it.kind.toString() == "CLIENT"}
        item.amount == 11.25
        def item2 = list.find{it.kind.toString() == "VENDOR"}
        item2.amount == 10.00
    }

    void "projections with is not null"() {
        setup:
        QueryArgs args = QueryArgs.of(
            q:[kind:'$isNotNull', amount:'$isNotNull'],
            projections: [kind:'group', amount:'sum']
        )

        when:
        MangoDetachedCriteria criteria = KitchenSink.repo.query(args)
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT kitchenSink.kind as kind, SUM(kitchenSink.amount) as amount_sum
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.kind IS NOT NULL  AND kitchenSink.amount IS NOT NULL )
            GROUP BY kitchenSink.kind
        ''')

        List<Map> list = criteria.mapList()
        list.size() == 3
    }

    void "projections having with between"() {
        when:
        MangoDetachedCriteria criteria = KitchenSink.repo.query(
            projections: [localDate:'group', amount:'sum'],
            q:[
                localDate:['$between':[ "2023-01-01", "2023-01-07" ]],
                localDateTime:['$between':[ "2023-01-01", "2023-01-07" ]]
            ]
        )
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:
        query
        query.trim() == strip('''
            SELECT kitchenSink.localDate as localDate, SUM(kitchenSink.amount) as amount_sum
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE ((kitchenSink.localDate >= :p1 AND kitchenSink.localDate <= :p2)
            AND (kitchenSink.localDateTime >= :p3 AND kitchenSink.localDateTime <= :p4))
            GROUP BY kitchenSink.localDate
        ''')

        List<Map> list = criteria.mapList()
        //XXX fix this one, query above needs to make sense
        list.size() == 0
    }

    void "projections having criteria map with named alias and dot condition"() {
        given: "Some criteria"

        def criteria = KitchenSink.query (
            projections: ['ext.totalDue as totalDue':'sum', 'kind':'group'],
            q: [
                'ext.id': 2,
                'thingId': 2,
                inactive: true,
                'totalDue.$lt':100.0
            ],
            sort:[totalDue:'asc']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''
            SELECT SUM(kitchenSink.ext.totalDue) as totalDue, kitchenSink.kind as kind
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            WHERE (kitchenSink.ext.id=:p1 AND kitchenSink.thing.id=:p2 AND kitchenSink.inactive=:p3)
            GROUP BY kitchenSink.kind
            HAVING (SUM(kitchenSink.ext.totalDue) < :p4)
            ORDER BY totalDue ASC
        ''')

        queryInfo.paramMap == [p1: 2, p2: 2, p3: true, p4: 100]

        List<Map> list = criteria.mapList()
        list.size() == 1
        list[0].totalDue == 20.50
    }

    void "projections with sum alias same name as property"() {
        given: "Some criteria"

        def criteria = KitchenSink.query (
            projections: ['ext.totalDue as amount':'sum', 'kind':'group'],
            q: [
                'amount.$gt':100.0
            ],
            sort:[amount:'DESC']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''
            SELECT SUM(kitchenSink.ext.totalDue) as amount, kitchenSink.kind as kind
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            GROUP BY kitchenSink.kind
            HAVING (SUM(kitchenSink.ext.totalDue) > :p1)
            ORDER BY amount DESC
        ''')

        queryInfo.paramMap == [p1: 100.0]

        List<Map> list = criteria.mapList()
        list.size() == 2
        list[0].amount == 287.0
        list[0].amount  > list[1].amount
    }

    void "Test projections simple with aliases"() {
        given:"Some criteria"
        def criteria = KitchenSink.query (
            projections: [
                'amount as x':'sum',
                'kind as y':'group',
                "ext.name as name": 'group'
            ]
        )

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""
        SELECT SUM(kitchenSink.amount) as x, kitchenSink.kind as y, kitchenSink.ext.name as name
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        GROUP BY kitchenSink.kind,kitchenSink.ext.name
        """)
    }

    void "Test projections with min max avg and count"() {
        given:"Some criteria"
        def criteria = KitchenSink.query (
            projections: [
                'id as cnt':'count',
                'amount as maxam':'max',
                'amount as minam':'min',
                "amount as avgam": 'avg',
                "kind as kindGroup": 'group'
            ]
        )

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""
            SELECT COUNT(DISTINCT kitchenSink.id) as cnt, MAX(kitchenSink.amount) as maxam, MIN(kitchenSink.amount) as minam,
            AVG(kitchenSink.amount) as avgam, kitchenSink.kind as kindGroup
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            GROUP BY kitchenSink.kind
        """)

        List<Map> list = criteria.mapList()
        list.size() == 3
        list[0].cnt == 4
        list[0].avgam == 7.5
        list[0].minam == 3.75
        list[0].maxam == 11.25
        list[0].kindGroup.toString() == 'CLIENT'
    }

    void "Test projections with joins"() {
        given:"Some criteria"
        // def criteria = KitchenSink.query {
        //     max('sinkLink.amount as maxam')
        //     groupBy("ext.name as name")
        //     sinkLink {
        //         gt("amount", 0.0)
        //         eq("kind", KitchenSink.Kind.CLIENT)
        //     }
        // }

        def criteria = KitchenSink.query (
            projections: [
                'sinkLink.amount as maxam':'max',
                'ext.name as name':'group'
            ],
            q: [
                "sinkLink.amount": ['$gt':0],
                "sinkLink.kind": "CLIENT",
            ]
        )

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT new map( MAX(kitchenSink.sinkLink.amount) as maxam, kitchenSink.ext.name as name )
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE (kitchenSink.sinkLink.amount > :p1 AND kitchenSink.sinkLink.kind=:p2)
        GROUP BY kitchenSink.ext.name
        """)
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
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1)'
    }
}
