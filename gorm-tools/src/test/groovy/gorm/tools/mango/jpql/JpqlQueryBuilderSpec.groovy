package gorm.tools.mango.jpql

import org.hibernate.engine.jdbc.internal.BasicFormatterImpl
import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.DetachedCriteria
import org.springframework.dao.InvalidDataAccessResourceUsageException
import spock.lang.Specification
import yakworks.gorm.config.GormConfig
import yakworks.spring.AppCtx
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.unit.GormHibernateTest

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip

/**
 * Test for JPA builder
 */
class JpqlQueryBuilderSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [KitchenSink]

    @Autowired
    GormConfig gormConfig

    static boolean compareQuery(String hql, String expected){
        assert formatAndStrip(hql) == formatAndStrip(expected)
        return true
    }

    void "Test update query with ilike criterion"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'amount', 10.0
            ilike 'name', 'Sink1'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        builder.enableDialectFunctions(true)
        def queryInfo = builder.buildUpdate(name:"SinkUp")

        then:"The query is valid"
        compareQuery(queryInfo.query, '''\
            UPDATE yakworks.testing.gorm.model.KitchenSink kitchenSink
            SET kitchenSink.name=:p1
            WHERE kitchenSink.amount=:p2 AND flike(kitchenSink.name, :p3 ) = true
        ''')
    }

    void "Test update query with subquery"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            notIn("amount",
                new DetachedCriteria(KitchenSink).build {
                    eq('name', 'Simpson')
                }.distinct('amount')
            )
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        def queryInfo = builder.buildUpdate(name:"SinkUp")

        then:"The query is valid"
        compareQuery(queryInfo.query,
            '''UPDATE yakworks.testing.gorm.model.KitchenSink kitchenSink SET kitchenSink.name=:p1
               WHERE kitchenSink.amount NOT IN (
                SELECT DISTINCT kitchenSink1.amount as amount
                FROM yakworks.testing.gorm.model.KitchenSink kitchenSink1 WHERE kitchenSink1.name=:p2
               )
            ''')
        queryInfo.parameters == ["SinkUp", "Simpson"]

    }

    void "Test update query with exists"() {
        given:
        def existsQuery = SinkExt.query {
            eqProperty("kitchenParent.id", "kitchenSink_.id")
        }.id()

        def criteria = KitchenSink.query {
           exists existsQuery
        }

        when:
        def builder = JpqlQueryBuilder.of(criteria)
        def queryInfo = builder.buildUpdate(name:"SinkUp")

        then:
        noExceptionThrown()
    }

    void "Test exception is thrown in join with delete"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            ext {
                eq 'name', 'SinkExt1'
            }
            eq 'name', 'SinkExt1'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        builder.buildDelete()

        then:"The query throws an exception"
        def e = thrown(InvalidDataAccessResourceUsageException)
        e.message == 'Joins cannot be used in a DELETE or UPDATE operation'

    }

    void "Test build update property natural ordering and hibernate compatible"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'name', 'Sink1'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        //builder.hibernateCompatible = true
        def queryInfo = builder.buildUpdate(name:"SinkUp", amount:30)

        then:"The query is valid"
        queryInfo.query != null
        compareQuery(queryInfo.query,
            'UPDATE yakworks.testing.gorm.model.KitchenSink kitchenSink SET kitchenSink.amount=:p1, kitchenSink.name=:p2 WHERE kitchenSink.name=:p3'
        )
        queryInfo.parameters == [30,"SinkUp", "Sink1"]
    }

    void "Test build update property natural ordering"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'name', 'Bob'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        def queryInfo = builder.buildUpdate(name:"SinkUp", amount:30.0)

        then:"The query is valid"
        queryInfo.query != null
        queryInfo.query == 'UPDATE yakworks.testing.gorm.model.KitchenSink kitchenSink SET kitchenSink.amount=:p1, kitchenSink.name=:p2 WHERE kitchenSink.name=:p3'
        queryInfo.parameters == [30.0,"SinkUp", "Bob"]
    }

    void "Test build update"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'name', 'Bob'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        def queryInfo = builder.buildUpdate(amount:30)

        then:"The query is valid"
        queryInfo.query != null
        queryInfo.query == 'UPDATE yakworks.testing.gorm.model.KitchenSink kitchenSink SET kitchenSink.amount=:p1 WHERE kitchenSink.name=:p2'
        queryInfo.parameters == [30, "Bob"]
    }

    void "Test build delete"() {
        given:"Some criteria"
        def criteria = KitchenSink.query {
            eq 'name', 'Bob'
        }

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        def queryInfo = builder.buildDelete()

        then:"The query is valid"
        queryInfo.query != null
        queryInfo.query == 'DELETE yakworks.testing.gorm.model.KitchenSink kitchenSink WHERE kitchenSink.name=:p1'
        queryInfo.parameters == ["Bob"]
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
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE kitchenSink.name=:p1'
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
        query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE kitchenSink.name=:p1'
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
        queryInfo.query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink WHERE (kitchenSink.name=:p1 OR kitchenSink.name=:p2)'
        queryInfo.parameters == ['Bob', 'Fred']

    }

    void "Test build DELETE with an empty criteria or build {}"() {
        given:
        def criteria = KitchenSink.query{}

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        final queryInfo = builder.buildDelete()

        then: "The query is valid"
        queryInfo.query!=null
        queryInfo.query == 'DELETE yakworks.testing.gorm.model.KitchenSink kitchenSink'
        queryInfo.parameters == []
    }


    void "Test build SELECT with an empty criteria or build {}"() {
        given:
        def criteria = KitchenSink.query{}

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria)
        final queryInfo = builder.buildSelect()

        then: "The query is valid"
        queryInfo.query!=null
        queryInfo.query == 'SELECT DISTINCT kitchenSink FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink'
        queryInfo.parameters == []
    }

    void "Test with alias"() {
        given:
        def criteria = KitchenSink.query(
            num: 123, name: 'blue'
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).entityAlias("foo")
        final queryInfo = builder.buildSelect()

        then: "The query is valid"
        compareQuery(queryInfo.query, """
            SELECT DISTINCT foo FROM yakworks.testing.gorm.model.KitchenSink AS foo
            WHERE foo.num=:p1 AND foo.name=:p2
        """)
        queryInfo.where == "foo.num=:p1 AND foo.name=:p2"
        queryInfo.parameters == ['123', 'blue']
    }

}
