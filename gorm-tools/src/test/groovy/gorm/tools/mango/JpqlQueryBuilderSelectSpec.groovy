package gorm.tools.mango

import gorm.tools.mango.jpql.JpqlQueryBuilder
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.testing.spring.AutowiredTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
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
        query == strip('''
            SELECT new map( SUM(kitchenSink.amount) as amount_sum,kitchenSink.kind as kind )
            FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink
            GROUP BY kitchenSink.kind
        ''')
        //
        // when:
        // List res = KitchenSink.executeQuery(query)
        //
        // then:
        // res.size() == 2
    }

    @Ignore
    void "Test build projections"() {
        given:"Some criteria"
        KitchenSink.generateDataList(20)
        def criteria = KitchenSink.query {
            sum('amount')
            groupBy('kind')
            ilike('name', 'sink%')
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query == strip('''
            SELECT SUM(kitchenSink.amount),kitchenSink.kind
            FROM yakworks.gorm.testing.model.KitchenSink AS kitchenSink
            WHERE (lower(kitchenSink.name) like lower(:p1))
            GROUP BY kitchenSink.kind
        ''')
        //
        // when:
        // List res = KitchenSink.executeQuery(query,queryInfo.parameters)

    }

}
