package yakworks.rally

import gorm.tools.utils.GormMetaUtils
import grails.core.support.proxy.ProxyHandler
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Hibernate

import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class HibernateProxyInitSpec extends Specification implements DataIntegrationTest {

    ProxyHandler proxyHandler

    void "load Org"() {
        expect:
        HibernateProxyAsserts.load()
    }

    void "HibernateProxyAsserts"() {
        expect:
        def proxy = Org.load(2)
        // HibernateProxyAsserts.assertIdCheckStaysProxy(proxy, proxyHandler)
        assert proxy
    }

    void "calling id prop should not unwrap proxy"() {
        when:
        def proxy = Org.load(2)

        then: "load returns a proxy"
        proxy.id
        !Hibernate.isInitialized(proxy)
        proxy.getId() == GormMetaUtils.getId(proxy)
        // getId should also not unwrap the proxy
        !Hibernate.isInitialized(proxy)
        !proxyHandler.isInitialized(proxy)

        //dirty check should not trigger it but it does, most any method call does
        //proxy.isAttached() will trigger it too
        proxy.isDirty() == false
        //FIXME change isdirty so it doesnt trigger it
        Hibernate.isInitialized(proxy)
    }

    void "verify association not initialized on id prop check"() {
        when:
        Org org = Org.get(4)
        //org.info.isDirty()

        then:
        org.info
        //access does not init
        !GrailsHibernateUtil.isInitialized(org, "info")
        !Hibernate.isInitialized(org.info)

        //id does not init
        org.infoId == 4
        org.info.id == 4
        !GrailsHibernateUtil.isInitialized(org, "info")
        !Hibernate.isInitialized(org.info)

        //getId does not init
        org.info.getId() == 4
        !GrailsHibernateUtil.isInitialized(org, "info")
        !Hibernate.isInitialized(org.info)

        // id check does not init it
        // org.info.id == 4
        !GrailsHibernateUtil.isInitialized(org, "info")
        !Hibernate.isInitialized(org.info)
    }

    void "verify association not initialized on getId() prop check"() {
        when:
        Org org = Org.get(4)
        //org.info.isDirty()

        then:
        org.info.getId() == 4
        !Hibernate.isInitialized(org.info)
        !GrailsHibernateUtil.isInitialized(org, "info")

        org.info.id == 4
        !Hibernate.isInitialized(org.info)
        !GrailsHibernateUtil.isInitialized(org, "info")
    }

    void "verify association not initialized on assocId prop check"() {
        when:
        Org org = Org.get(2)

        then:
        !Hibernate.isInitialized(org.info)
        org.infoId == 2
        !Hibernate.isInitialized(org.info)
        //GrailsHibernateUtil is just an alternative to Hibernate way to check and sanity check
        // !GrailsHibernateUtil.isInitialized(org, "info")
    }

    void "verify association not initialized on validate"() {
        when:
        Org org = Org.get(4)
        org.name = 'foo'

        then:
        //!org.ext.isDirty() //this should not initialize the proxy
        !Hibernate.isInitialized(org.info)
        !GrailsHibernateUtil.isInitialized(org, "info")
        org.validate()
        !Hibernate.isInitialized(org.info)
        !GrailsHibernateUtil.isInitialized(org, "info")
    }

    void "verify association not initialized on persist"() {
        when:
        Org org = Org.get(2)
        org.name = "updated"
        org.persist(flush: true)

        then:
        !GrailsHibernateUtil.isInitialized(org,"info")
        !Hibernate.isInitialized(org.info)
    }
}
