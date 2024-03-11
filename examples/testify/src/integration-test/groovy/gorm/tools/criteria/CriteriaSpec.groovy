package gorm.tools.criteria

import org.hibernate.Criteria
import org.hibernate.FetchMode
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

//Old criteria queries
@Integration
@Rollback
class CriteriaSpec extends Specification {

    def "test createCriteria for nested property added by CreateCriteriaSupport trait"() {
        when:
        List list = Org.createCriteria().list{
            location {
                eq "city", "City12"
            }
        }
        then:
        list.size() == 1
    }

    def "test nested query"() {
        when:
        List list = Org.query {
            eq "location.city", "City22"
        }.list()

        then:
        list.size() == 1
    }

    void "test in subquery"() {
        when:
        def subQuery = Org.query {
            gt "flex.num2", 2.0
            projections {
                property("flex.id")
            }
        }
        //just a cooked up query - select * from org where flex.id in (select flex.id from org where flex.num > 2.0)
        def query = Org.query {
            "in" "flex.id", subQuery
        }

        List result = query.list()

        then:
        noExceptionThrown()
        result
    }

    void "test in subquery as closure"() {
        when:

        def query = Org.query {
            "in" "flex.id", {
                gt "flex.num2", 2.0
                projections {
                    property("flex.id")
                }
            }
        }

        List result = query.list()

        then:
        noExceptionThrown()
        result
    }

    def "hibernate directly to criteria query"() {
        when:
        Criteria cr
        Org.withSession { session ->
            assert session instanceof org.hibernate.Session
            cr = session.createCriteria(Org.class);
        }

        // cr.createAlias("info", "info", JoinType.LEFT_OUTER_JOIN)
        //     .add(Restrictions.eq("info.phone", "1-800-50"))
        //     .setFetchMode("info", FetchMode.LAZY)

        cr.createAlias("contact", "contact", JoinType.LEFT_OUTER_JOIN)
            .createAlias("contact.location", "contact_location", JoinType.LEFT_OUTER_JOIN)
            .add(Restrictions.eq("contact_location.city", "City10"))
            .setFetchMode("info", FetchMode.LAZY)

        // cr.createCriteria("location").add(Restrictions.eq("city", "City12"))
        //     .setFetchMode("location", FetchMode.JOIN)
        List results = cr.list()

        then:
        "foo"=='foo'
        results.size() == 1
        // HibernateGormStaticApi<D> staticApi
        // Criteria cr = session.createCriteria(Org.class);
        // List results = cr.list();
        // then:
        // list.size() == 1
    }
}
