package gorm.tools

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

}
