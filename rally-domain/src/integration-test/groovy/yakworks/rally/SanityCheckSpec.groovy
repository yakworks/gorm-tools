package yakworks.rally

import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.integration.DomainIntTest

// Use as a simple to test when trying to see why application context has problem on init
@Integration
@Rollback
class SanityCheckSpec extends Specification implements DomainIntTest {

    void "WTF"() {
        expect:
        Org.load(2)
    }

    def "test new org"() {
        expect:
        def o = new Org(num: '123', name: 'foo', type: OrgType.Customer)
        o.persist(flush: true)
    }
}
