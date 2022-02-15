package yakworks.rally.orgs

import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.SecurityTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactRepo

class ContactRepoSpec extends Specification implements DomainRepoTest<Contact>, SecurityTest {

    ContactRepo contactRepo

    def setupSpec() {
        mockDomains(Org)
    }

    void "test lookup by num"() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Contact contact = build(firstName: 'foo', num: 'foo', org:org)
        contact.persist()

        then:
        Contact c = contactRepo.lookup(num:'foo')
        assert c

    }

}
