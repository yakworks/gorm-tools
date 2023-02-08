package yakworks.rally.orgs

import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactSourceRepo
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.SecurityTest

import javax.inject.Inject

class ContactSourceRepoSpec extends Specification implements DataRepoTest, SecurityTest {
    static List entityClasses = [Contact, Org, ContactSource]
    @Inject ContactSourceRepo contactSourceRepo


    void "test lookup by sourceId"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Contact contact = build(Contact, firstName: 'foo', num: 'foo', org:org).persist()
        ContactSource source = build(ContactSource, sourceId: '123', contact:contact).persist()

        when:
        ContactSource result = contactSourceRepo.lookup(sourceId:'123')

        then:
        result
        result.id == source.id
    }

}
