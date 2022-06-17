package yakworks.rally

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Hibernate

import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class HibernateProxyInitSpec extends Specification implements DataIntegrationTest {

    void "calling id prop should not unwrap proxy"() {
        when:
        def proxy = Org.load(2)

        then: "load returns a proxy"
        !Hibernate.isInitialized(proxy)
        proxy.getId()
        // getId should also not unwrap the proxy
        !Hibernate.isInitialized(proxy)
        //def proxyHandler = Org.getGormPersistentEntity().mappingContext.proxyHandler
        // !proxyHandler.isInitialized(proxy)
        // id should also not unwrap the proxy
        // proxy.id
        // !Hibernate.isInitialized(proxy)

    }

    void "verify association not initialized on id prop check"() {
        when:
        Org org = Org.get(4)
        //org.info.isDirty()

        then:
        org.info
        //access does not init
        !GrailsHibernateUtil.isInitialized(org, "info")

        //id does not init
        org.infoId == 4
        !GrailsHibernateUtil.isInitialized(org, "info")

        //getId does not init
        org.info.getId() == 4
        !GrailsHibernateUtil.isInitialized(org, "info")

        //id inits it
        // org.info.id == 4
        // !GrailsHibernateUtil.isInitialized(org, "info")
    }

    void "verify association not initialized on getId() prop check"() {
        when:
        Org org = Org.get(4)
        //org.info.isDirty()

        then:
        org.info.getId() == 4
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

    }
}
