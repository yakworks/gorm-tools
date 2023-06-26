package gorm.tools

import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink

// Use as a simple to test when trying to see why application context has problem on init
@Integration
@Rollback
class SanityCheckSpec extends Specification implements DataIntegrationTest {

    void "WTF"() {
        expect:
        KitchenSink.load(2)
    }

    void "check isDirty throws error"() {
        setup:
        Org org = Org.get(1)

        expect:
        org

        when:
        org.flex = new OrgFlex()
        org.num = "new num"

        and:
        org.isDirty("num")

        then:
        noExceptionThrown()
    }

}
