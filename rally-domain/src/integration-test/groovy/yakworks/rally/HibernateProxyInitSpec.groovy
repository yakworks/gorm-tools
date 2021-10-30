package yakworks.rally

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Hibernate

import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class HibernateProxyInitSpec extends Specification implements DataIntegrationTest {

    void "calling getId() should not unwrap proxy"() {
        when:
        def proxy = Org.load(1)

        then: "load returns a proxy"
        !Hibernate.isInitialized(proxy)

        when:
        proxy.id

        then: "getId should also not unwrap the proxy"
        !Hibernate.isInitialized(proxy)
    }

    void "verify association not initialized on id check"() {
        when:
        Org org = Org.get(4)
        //org.info.isDirty()

        then:
        org.info.id == 4
        !Hibernate.isInitialized(org.info)
        !GrailsHibernateUtil.isInitialized(org, "info")

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
}
