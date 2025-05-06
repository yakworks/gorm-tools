package yakworks.rally.orgs

import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.rally.orgs.repo.ContactSourceRepo
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import javax.inject.Inject

class ContactSourceRepoSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Contact, Org, ContactSource, PartitionOrg]
    static List springBeans = [OrgProps]

    @Inject ContactSourceRepo contactSourceRepo


    void "test lookup by sourceId"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        Contact contact = Contact.create(firstName: 'foo', num: 'foo', orgId:org.id).persist()
        ContactSource source = build(ContactSource, sourceId: '123', contact:contact).persist()

        when:
        ContactSource result = contactSourceRepo.lookup(sourceId:'123')

        then:
        result
        result.id == source.id
    }

    void "test findContactIdBySourceId"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        Contact contact = Contact.create( firstName: 'foo', num: 'foo', orgId:org.id, sourceId: '123')
        flushAndClear()

        when:
        Long cid = contactSourceRepo.findContactIdBySourceId('123')

        then:
        cid
        cid == contact.id
    }

}
