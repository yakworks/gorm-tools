package gorm.tools.criteria

import org.grails.orm.hibernate.HibernateGormStaticApi
import org.grails.orm.hibernate.HibernateSession
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
