package testing

import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy

import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import spock.lang.Ignore
import spock.lang.Specification
import yakworks.taskify.domain.Org

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
        org.ext.id == 4
        !Hibernate.isInitialized(org.ext)
        !GrailsHibernateUtil.isInitialized(org, "ext")

    }

    void "verify association not initialized on validate"() {
        when:
        Org org = Org.get(4)
        org.name = 'foo'

        then:
        //!org.ext.isDirty() //this should not initialize the proxy
        !Hibernate.isInitialized(org.ext)
        !GrailsHibernateUtil.isInitialized(org, "ext")
        org.validate()
        !Hibernate.isInitialized(org.ext)
        !GrailsHibernateUtil.isInitialized(org, "ext")
    }
}
