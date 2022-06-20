package gorm.tools


import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Hibernate
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.model.KitchenSink
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class ManagedEntitySinkSpec extends Specification implements DataIntegrationTest {

    //When adding @ManagedEntity on KitchenSink and Thing, thing is loaded, even when its set to lazy.
    void "getting should return not proxy"() {
        when:
        // createOrg()
        flushAndClear()
        def proxy = KitchenSink.load(1)

        then: "load returns a proxy"
        proxy
        // proxy.thing
        // proxy.comments
        // proxy.ext
    }

}
