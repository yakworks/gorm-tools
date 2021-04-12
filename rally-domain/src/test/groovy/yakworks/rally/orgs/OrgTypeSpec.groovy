package yakworks.rally.orgs

import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import yakworks.rally.orgs.model.OrgTypeSetup
import spock.lang.Specification

class OrgTypeSpec extends Specification implements DomainRepoTest<OrgTypeSetup>, SecurityTest {
    //auto runs DomainRepoCrudSpec tests
    void "CRUD tests"() {
        expect:
        createEntity().id
        persistEntity().id
        updateEntity().version > 0
        removeEntity()
    }
}
