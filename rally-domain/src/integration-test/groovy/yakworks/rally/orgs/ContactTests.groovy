package yakworks.rally.orgs

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.problem.data.DataProblem
import yakworks.problem.data.DataProblemCodes
import yakworks.problem.data.DataProblemException
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactRepo

@Integration
@Rollback
class ContactTests extends Specification implements DomainIntTest {

    ContactRepo contactRepo

    def "listActive should only return active contacts"() {
        when:
        def result = Contact.listActive(9)
        def primary = Org.get(9).contact
        def secondary = Contact.findByNum("secondary9")

        then:
        result.contains(primary)
        result.contains(secondary)

        when:
        secondary.inactive = true
        secondary.persist(flush:true)

        result = Contact.listActive(9)

        then:
        !result.contains(secondary)
    }

    def testDeleteFailure_ForLoggedInUserContact(){

        expect:
        secService.userId == 1

        when:
        Contact contact = Contact.get(1)

        then:
        contact != null

        when:
        contactRepo.remove(contact)

        then:
        def ex = thrown(DataProblemException)
        ex.problem instanceof DataProblem
        ex.code == DataProblemCodes.ReferenceKey.code
    }

    def testDelete() {
        setup:
        Org org = Org.first()
        Contact contact = new Contact(num: "T1", name: "T1", org: org, firstName: "T1").persist()

        ContactEmail email = new ContactEmail(contact: contact, address: "test").persist()
        ContactFlex flex = new ContactFlex(id: contact.id, text1: "test").persist()
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
        def con = Org.get(50).contact
        con.user = null
        con.persist(flush:true)
        //contact 50 is key contact
        Org.get(50).contact.remove()

        then:
        def ex = thrown(DataProblemException)
        ex.problem instanceof DataProblem
        ex.code == DataProblemCodes.ReferenceKey.code
    }

    void "test create Contact with org lookup by orgSource"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Org.repo.createSource(org)
        org.persist(flush: true)

        Map params = [firstName:'Peter', email:'abc@walmart.com']
        params.org = [source: [sourceId: 'foo', orgType: OrgType.Customer.name()]]


        when:
        def entity = Contact.create(params)
        flushAndClear()
        def contact = Contact.get(entity.id)

        then:
        contact.org.num == "foo"

    }

}