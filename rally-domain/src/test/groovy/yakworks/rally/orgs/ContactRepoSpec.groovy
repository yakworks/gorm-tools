package yakworks.rally.orgs

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.testing.gorm.SecurityTest
import yakworks.testing.gorm.unit.DataRepoTest

class ContactRepoSpec extends Specification implements DataRepoTest, SecurityTest {
    static List entityClasses = [Contact, Org]
    @Autowired ContactRepo contactRepo

    void "test lookup by num"() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Contact contact = build(Contact, firstName: 'foo', num: 'foo', org:org)
        contact.persist()

        then:
        Contact c = contactRepo.lookup(num:'foo')
        assert c

    }

}
