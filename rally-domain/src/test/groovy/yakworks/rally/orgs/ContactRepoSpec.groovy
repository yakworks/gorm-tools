package yakworks.rally.orgs

import gorm.tools.model.SourceType
import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import javax.inject.Inject

class ContactRepoSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Contact, Org, ContactSource, PartitionOrg]
    static List springBeans = [OrgProps]

    @Inject ContactRepo contactRepo

    void "create without source"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        assert org.id

        Map data = [name: "C1", firstName: "C1", orgId: org.id,]

        when:
        Contact contact = Contact.create(data)
        flushAndClear()
        contact.refresh()

        then:
        noExceptionThrown()
        contact
        contact.source == null

        and:
        ContactSource.countByContactId(contact.id) == 0
        contact.org
        contact.orgId == org.id
        contact.org.contact == null
    }

    void "create and set as primary contact"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        Map data = [name: "C1", firstName: "C1", orgId: org.id, isPrimary:true]

        when:
        Contact contact = Contact.create(data)
        flushAndClear()
        contact.refresh()

        then:
        noExceptionThrown()
        contact
        contact.org.contact == contact
    }

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
        flush()
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

    /**
     The problem with isDirty is : isDirty throws transient object exception if a new association is set on instance.
     Its replaced with "hasChanged"

     Here's how hasChanged works.

     1.Dirty status is stored in $changedProperties which is null initially
     2.instance.hasChanged() returns true if $changedProperties is null (thts strange)
     3."New instance is always dirty untill saved" : That is because it has $changedProperties = null
     4."New instance is not dirty after save, even without flush" :
        That is because ClosureEventTriggeringInterceptor.activateDirtyChecking sets  $changedProperties to empty list if its null.
        ClosureEventTriggeringInterceptor gets called during hibernate's saveOrUpdate event` that even fires even without flush
     5."Existing instance is dirty untill saved with flush"
        In this case dirty status is reset by GrailsEntityDirtinessStrategy.resetDirty which implements a CustomEntityDirtinessStrategy
        Dirty status does not get reset during  saveOrUpdate event like in the case of new, because, ClosureEventTriggeringInterceptor does that only if changedProperties is null. and its not null in this case.

     See https://github.com/yakworks/gorm-tools/pull/696
     **/
    void "set location on existing contact"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer).persist()
        Contact contact = Contact.create(firstName: 'foo', num: 'foo', orgId:org.id, sourceId:"123")
        flushAndClear()

        when:
        contact = Contact.get(contact.id)

        then:
        !contact.location

        when:
        contact.location = new Location()
        //contact.isDirty() this would throw TransientObjectException, and so is replaced with hasChanged
        contact.hasChanged()

        then:
        noExceptionThrown()
    }

}
