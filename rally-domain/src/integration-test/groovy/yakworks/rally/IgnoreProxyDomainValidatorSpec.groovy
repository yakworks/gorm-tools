package yakworks.rally

import org.hibernate.Hibernate

import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex

@Integration
@Rollback
class IgnoreProxyDomainValidatorSpec extends Specification implements DataIntegrationTest {

    void "calling getId() should not unwrap proxy"() {
        when:
        def proxy = Org.load(2)

        then: "load returns a proxy"
        !Hibernate.isInitialized(proxy)

        when:
        proxy.id

        then: "getId should also not unwrap the proxy"
        !Hibernate.isInitialized(proxy)
    }

    void "verify association not initialized on assocId prop check"() {
        when:
        Org org = Org.get(2)
        //org.info.isDirty()

        then:
        !Hibernate.isInitialized(org.info)
        org.infoId == 2
        //GrailsHibernateUtil is just an alternative to Hibernate way to check and sanity check
        !GrailsHibernateUtil.isInitialized(org, "info")
    }

    void "verify association not initialized on id check"() {
        when:
        Org org = Org.get(2)
        //org.info.isDirty()

        then:
        !Hibernate.isInitialized(org.info)
        org.info.id == 2
        //GrailsHibernateUtil is just an alternative to Hibernate way to check and sanity check
        !GrailsHibernateUtil.isInitialized(org, "info")

    }

    void "verify association not initialized on persist"() {
        when:
        Org org = Org.get(2)
        org.name = "updated"
        org.persist(flush: true)

        then:
        !GrailsHibernateUtil.isInitialized(org,"info")

    }

    void "verify proxies are not initialized during validation"() {
        setup:
        Org org = Org.get(2)
        org.flex = new OrgFlex(text1: "test") //.persist(flush: true)
        org.persist(flush: true)

        //clear session cache, so the object is not cached, and next load will reload it from db resulting in flex being lazy loaded.
        flushAndClear()

        expect:

        when:
        org = Org.get(org.id)

        then:

        GrailsHibernateUtil.isInitialized(org,"flex") == false

        when:
        org.name = "updated"
        org.persist(flush: true)

        then:
        GrailsHibernateUtil.isInitialized(org,"flex") == false
    }
}
