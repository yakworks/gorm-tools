package yakworks.rally.orgs

import gorm.tools.model.SourceType
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import javax.inject.Inject

class ContactRepoSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Contact, Org, ContactSource]
    @Inject ContactRepo contactRepo


    void "create with source"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        assert org.id

        Map data = [
            name: "C1",
            firstName: "C1",
            orgId: org.id,
            source:"test",
            sourceType : SourceType.App,
            sourceId: "C1-SID"
        ]

        when:
        Contact contact = Contact.create(data)
        flushAndClear()
        contact.refresh()

        then:
        contact
        contact.source

        and:
        contact.source.sourceId == "C1-SID"

        and:
        contact.id == ContactSource.repo.findContactIdBySourceId("C1-SID")

    }

    void "lookup by num"() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        Contact contact = Contact.create( firstName: 'foo', num: 'foo', orgId:org.id)
        Contact c = contactRepo.lookup(num:'foo')

        then:
        c
        c.id == contact.id
    }

    void "lookup by sourceId"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        Contact contact = Contact.create(firstName: 'foo', num: 'foo', orgId:org.id, sourceId:"123")
        flush()

        expect:
        contact.source
        contact.source.sourceId == "123"

        when:
        Contact c = contactRepo.lookup(sourceId:'123')

        then:
        c
        c.id == contact.id

        when: "Lookup from sources collection"
        c = contactRepo.lookup(source:[sourceId:'123'])

        then:
        c
        c.id == contact.id
    }

}
