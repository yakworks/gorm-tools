package yakworks.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.gorm.model.AppUser
import yakworks.testing.gorm.integration.DomainIntTest

// Use as a simple to test when trying to see why application context has problem on init
@Integration
@Rollback
class SanityCheckSpec extends Specification implements DomainIntTest {

    void "WTF?"() {
        expect:
        AppUser.load(1)
    }

}
