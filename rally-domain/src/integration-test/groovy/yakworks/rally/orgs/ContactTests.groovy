package yakworks.rally.orgs

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.repo.ContactRepo

@Integration
@Rollback
class ContactTests extends Specification implements DataIntegrationTest {

    ContactRepo contactRepo

    def testEquals() {
        when:
        def contact = Contact.get(51)

        then:
        contact != null

        when:
        def contact2 = Contact.get(51)

        then:
        contact2 != null
        contact.equals(contact2)

        when:
        contact2 = Contact.get(52)

        then:
        !contact.equals(contact2)

        when:
        // diff class
        def org = Org.get(101)

        then:
        !contact.equals(org)

        when:
        // null
        def contactNull = Contact.get(-987)

        then:
        !contact.equals(contactNull)
    }

    def testHashCode() {
        when:
        def contact = Contact.get(51)
        int contactHash = contact.hashCode()

        def contact2 = Contact.get(51)
        int contact2Hash = contact2.hashCode()

        then:
        contactHash == contact2Hash

        when:
        contact2 = Contact.get(52)
        contact2Hash = contact2.hashCode()

        then:
        contactHash != contact2Hash
    }

    def testListActive() {
        when:
        def result = Contact.listActive(205)

        then:
        result.contains(Contact.get(52))

        when:
        def totest = Contact.get(52)
        totest.email = 'bb@greenbill.com'
        totest.inactive = true
        totest.persist(flush:true)
        result = Contact.listActive(205)

        then:
        !result.contains(Contact.get(52))
    }

    def testDeleteFailure_ForLoggedInUserContact(){

        expect:
        secService.userId != null

        when:
        Contact contact = Contact.findById(secService.userId)

        then:
        contact != null

        when:
        contactRepo.remove(contact)

        then:
        thrown(EntityValidationException)
    }

    def testDelete() {
        setup:
        Org org = Org.first()
        Contact contact = new Contact(num: "T1", name: "T1", org: org, firstName: "T1").persist()

        ContactEmail email = new ContactEmail(contact: contact, address: "test").persist()
        ContactFlex flex = new ContactFlex(contact: contact, text1: "test").persist()
        ContactPhone phone = new ContactPhone(contact: contact, num: "123").persist()
        ContactSource source = new ContactSource(contact: contact, source:"9ci", sourceType: "App", sourceId: "x").persist()
        Location l = Location.first()
        l.contact = contact
        l.persist()

        contact.flex = flex
        contact.addToEmails(email)
        contact.addToPhones(phone)
        contact.addToSources(source)
        contact.persist()

        flushAndClear()

        when:
        contactRepo.remove(contact)
        flushAndClear()
        l.refresh()

        then:
        l.contact == null
        Contact.get(contact.id) == null
        ContactEmail.get(email.id) == null
        ContactFlex.get(flex.id) == null
        ContactPhone.get(phone.id) == null
        ContactSource.get(source.id) == null
    }

    void "test delete contact fails when its primary contact for org"() {
        when:
        def con = Contact.get(50)
        con.user = null
        con.persist(flush:true)
        //contact 50 is key contact
        Contact.get(50).remove()

        then:
        EntityValidationException e = thrown()
        e.code == "contact.not.deleted.iskey"
    }

    void "test contact email update : updates user.email"() {
        when:
        Contact cuser = Contact.get(2)
        cuser.email = "updated@email.com"
        cuser.persist()
        flush()

        then:
        cuser.user.email == "updated@email.com"

    }

}
