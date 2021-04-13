package yakworks.rally.activity

import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTypeSetup
import spock.lang.Specification

class ActivityContactSpec extends Specification implements DomainRepoTest<ActivityContact>, SecurityTest {

    void setupSpec() {
        mockDomains(Org, OrgTypeSetup)
    }

    void setup(){
        def ots = new OrgTypeSetup(name: 'Customer').persist(flush:true)
        assert ots.id == 1
    }

    void "CRUD tests"() {
        expect:
        createEntity().id
        persistEntity().id
        // updateEntity().version > 0
        removeEntity()
    }

}
