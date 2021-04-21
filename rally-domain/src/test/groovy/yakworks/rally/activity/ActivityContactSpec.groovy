package yakworks.rally.activity

import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.orgs.model.Org

class ActivityContactSpec extends Specification implements DomainRepoTest<ActivityContact>, SecurityTest {

    void setupSpec() {
        mockDomains(Org)
    }

    void "CRUD tests"() {
        expect:
        createEntity().id
        persistEntity().id
        // updateEntity().version > 0
        removeEntity()
    }

}
