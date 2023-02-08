package yakworks.rally.orgs

import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.testing.gorm.unit.DataRepoTest

import javax.inject.Inject

class ContactRepoSpec extends Specification implements DataRepoTest, SecurityTest {
    static List entityClasses = [Contact, Org, ContactSource]
    @Inject ContactRepo contactRepo

    void "lookup by num"() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Contact contact = build(Contact, firstName: 'foo', num: 'foo', org:org).persist()
        Contact c = contactRepo.lookup(num:'foo')

        then:
        c
        c.id == contact.id
    }

    void "lookup by sourceId"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Contact contact = build(Contact, firstName: 'foo', num: 'foo', org:org).persist()
        build(ContactSource, sourceId: '123', contact:contact).persist()

        when:
        Contact c = contactRepo.lookup(sourceId:'123')

        then:
        c
        c.id == contact.id
    }

}
