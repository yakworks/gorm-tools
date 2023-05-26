package yakworks.rally.orgs

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.DataProblemException
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
        currentUser.userId == 1

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
        Location l = Location.first()
        l.contact = contact
        l.persist()

        contact.flex = flex
        contact.addToEmails(email)
        contact.addToPhones(phone)
        contact.persist()

        flushAndClear()

        when:
        contactRepo.remove(contact)
        flushAndClear()

        then:
        Contact.get(contact.id) == null
        Location.get(l.id) == null

        //following associations should have been deleted through cascade as per mapping.
        ContactEmail.get(email.id) == null
        ContactFlex.get(flex.id) == null
        ContactPhone.get(phone.id) == null
        ContactSource.countByContactId(contact.id) == 0
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

    void "update contact with source"() {
        setup:
        jdbcTemplate.execute("CREATE UNIQUE INDEX ix_contactsource_sourceid_uniq ON ContactSource(sourceId)")
        Map data = [
            "num": "num",
            "name": "name",
            "email": "test@9ci.com",
            "companyId": 2,
            "orgId":2,
            "source": ["sourceId":"123"]
        ]

        when:
        Contact contact = Contact.create(data)
        flush()

        then:
        noExceptionThrown()
        contact.num == "num"
        contact.firstName == "name"
        contact.name == "name"
        contact.email == "test@9ci.com"
        contact.source
        contact.source.sourceId == "123"

        when:"update contact"
        data.firstName = "name2"
        data.email = "dev@9ci.com"
        contact = Contact.update(data)
        flush()

        then:
        noExceptionThrown()
        contact.name == "name2"
        contact.email == "dev@9ci.com"

        and:
        ContactSource.countByContactId(contact.id) == 1

        cleanup:
        jdbcTemplate.execute("DROP index ix_contactsource_sourceid_uniq")
    }

}
