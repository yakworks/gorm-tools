package gorm.tools.mango.jpql

import javax.persistence.criteria.JoinType

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
class JpqlQueryBuilderSelectClosureSpec extends Specification implements GormHibernateTest  {

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

    void "Test build simple select hibernate compatible"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'name', 'Bob'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        //builder.hibernateCompatible = true
        def query = builder.buildSelect().query

        then:"The query is valid"
        query != null
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1)'
    }

    void "Mango Map testing for adding map to an existing criteria instance"() {
        when:
        def criteria = KitchenSink.query(
            name: 'Bob'
        )
        def crit = criteria.where([name2: 'foo'])

        def query = JpqlQueryBuilder.of(crit).buildSelect().query

        then:"The query is valid"
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1 AND kitchenSink.name2=:p2)'
    }

    void "Mango Map testing for adding map with applyMapOrList"() {
        when:
        def crit = KitchenSink.query {
            eq 'name', 'Bob'
        }
        //THIS SHOWS that if dont use clone then its messed up in nesting
        new MangoBuilder().applyMapOrList(crit, [name2: 'foo'])

        //this will work
        //crit.eq('comments', 'com')
        //but this will not
        // crit.build {
        //     eq('comments', 'com')
        // }
        //if wanting to to add then use where and get its return for the clone
        crit = crit.where {
            eq('comments', 'com')
        }

        def query = JpqlQueryBuilder.of(crit).buildSelect().query

        then:"The query is not valid"
        query == strip("""\
        SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE (kitchenSink.name=:p1 AND kitchenSink.name2=:p2 AND kitchenSink.comments=:p3)
        """)
        //query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1 AND kitchenSink.name2=:p2)'
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

    void "Test projections simple aliasToMap"() {
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
        SELECT new map( SUM(kitchenSink.amount) as amount_sum, kitchenSink.kind as kind, kitchenSink.ext.name as ext_name )
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
        def builder = JpqlQueryBuilder.of(criteria) // .aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query != null
        query.trim() == strip('''\
        SELECT SUM(kitchenSink.sinkLink.amount) as sinkLink_amount_sum, kitchenSink.kind as kind
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

    }

    void "Test projections simple with aliases"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            sum('amount as x')
            groupBy('kind as y')
            groupBy("ext.name as name")
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT SUM(kitchenSink.amount) as x, kitchenSink.kind as y, kitchenSink.ext.name as name
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
            countDistinct('id as cnt')
            max('amount as maxam')
            min('amount as minam')
            avg('amount as avgam')
            groupBy('kind as y')
            groupBy("ext.name as name")
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT COUNT(DISTINCT kitchenSink.id) as cnt, MAX(kitchenSink.amount) as maxam, MIN(kitchenSink.amount) as minam,
        AVG(kitchenSink.amount) as avgam, kitchenSink.kind as y, kitchenSink.ext.name as name
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

    void "Test projections with joins"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            max('sinkLink.amount as maxam')
            groupBy("ext.name as name")
            sinkLink {
                gt("amount", 0.0)
                eq("kind", KitchenSink.Kind.CLIENT)
            }
            join("sinkLink", JoinType.LEFT)
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip("""\
        SELECT MAX(kitchenSink.sinkLink.amount) as maxam, kitchenSink.ext.name as name
        FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
        WHERE (kitchenSink.sinkLink.amount > :p1 AND kitchenSink.sinkLink.kind=:p2)
        GROUP BY kitchenSink.ext.name
        """)
        //
        // when:
        // List res = KitchenSink.executeQuery(query)
        //
        // then:
        // res.size() == 2
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

    void "Test select using property method"() {
        given:"Some criteria"

        def criteria = KitchenSink.query(null).property("id").property("name")

        //produces same thing if we do
        //def criteria = KitchenSink.query(null).distinct("id").distinct("name")

        // this does not work for some reason
        // def criteria = KitchenSink.query{
        //     property("id")
        //     property("name")
        // }

        when:
        def builder = JpqlQueryBuilder.of(criteria)
        //builder.hibernateCompatible = true
        JpqlQueryInfo queryInfo = builder.buildSelect()

        then:
        queryInfo.query == strip("""
            SELECT kitchenSink.id as id, kitchenSink.name as name
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            GROUP BY kitchenSink.id,kitchenSink.name
        """)

        List<Map> list = criteria.mapList()
        list.size() == 10
        list[0].id == 1
        list[0].name == 'Blue Cheese'
    }
}
