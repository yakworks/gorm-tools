package yakworks.rally.activity

import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org

class ActivityContactSpec extends Specification implements DomainRepoTest<ActivityContact>, SecurityTest {

    void setupSpec() {
        mockDomains(Activity, Contact, Org)
    }

    void "Create"() {
        when:
        def act = build(Activity)
        def c = build(Contact)
        def sc = ActivityContact.create(act, c)

        then:
        sc
    }

}
