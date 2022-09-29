package gorm.tools

import org.hibernate.Hibernate

import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink

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
        !Hibernate.isInitialized(proxy)
        proxy.name
        Hibernate.isInitialized(proxy)
        // proxy.thing
        // proxy.comments
        // proxy.ext
    }

}
