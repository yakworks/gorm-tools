package yakworks.rally.orgs

import gorm.tools.security.domain.AppUser
import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.TestDataJson
import gorm.tools.testing.unit.DomainRepoTest
import grails.buildtestdata.TestData
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTypeSetup
import yakworks.rally.testing.MockData

class ContactSpec extends Specification implements DomainRepoTest<Contact>, SecurityTest {

    void setupSpec(){
        mockDomains(AppUser, Org, OrgSource, OrgTypeSetup, Location, ContactPhone, ContactSource, ContactEmail)
    }

    @Override
    Map buildMap(Map args) {
        args.org = MockData.org()
        TestDataJson.buildMap(args, Contact)
    }

    // Contact build(Map args) {
    //     MockData.contact(args)
    // }

    Contact createContactWithUser(){
        Contact contact = MockData.contact([firstName: "John", lastName: 'Galt',  email: "al@9ci.io"])
        // AppUser user = TestData.build(AppUser, [password:"test"])
        AppUser user = new AppUser(username: contact.email, email: contact.email, password: 'foo')
        MockData.stamp(user)
        user.id = contact.id
        user.persist()
        contact.user = user
        contact
    }

    //show data table option 2
    def "test invalid email"() {
        when:
        MockData.contact(email: 'foo@bar.comx')
        // createEntity(email: 'foo@bar.comx')

        then:
        thrown(gorm.tools.repository.errors.EntityValidationException)
    }

    def testEquals() {
        when:
        Contact contact1 = build([:])
        Contact contact2 = build([:])

        then:
        !contact1.equals(contact2)
        contact1 != contact2

    }

    def testHashCode() {
        when:
        Contact contact = build([:])
        int contactHash = contact.hashCode()

        def contact2 = build([:])
        int contact2Hash = contact2.hashCode()

        then:
        contactHash != contact2Hash

    }

    def testUpdateContact_noUser(){
        when:
        // Checking firstName of contact before update
        Contact contact =  MockData.contact(firstName: "Robert")

        then:
        "Robert" == contact.firstName

        when:
        Map params = [id:contact.id, firstName:'Peter', email:'abc@walmart.com', tagForReminders:'on']
        Contact.update(params)

        Contact updatedContact = Contact.get(contact.id)

        then:
        params['firstName'] == updatedContact.firstName
        params['email'] == updatedContact.email
        contact.tagForReminders
    }


    def testUpdateContact_withUser(){
        when:
        Contact contact = MockData.createContactWithUser()

        then:
        contact.firstName == 'John'
        contact.user

        when:
        Map params = [id: contact.id, firstName: 'Peter', lastName: 'Rabbit', email: 'abc@walmart.com',
                      tagForReminders: 'on']
        Contact result = Contact.repo.update(params)

        then:
        result
        result.errors.allErrors.size() == 0

        when:
        // Checking firstName of contact after update
        Contact updatedContact = Contact.get(result.id)

        then:
        params['firstName'] == updatedContact.firstName
        params['email'] == updatedContact.email
        contact.tagForReminders
        updatedContact.name == 'Peter Rabbit'
        updatedContact.user.name == 'Peter Rabbit'
    }

    /*
    * TestCase for updating contact which is not a default contact without TagForReminder turned on
    */
    def testUpdateContact_WithoutTagForReminder(){
        when:
        Contact contact = MockData.createContactWithUser()

        then:
        "John" == contact.firstName

        when:
        Map params = [id:contact.id, firstName:'Peter', email:'abc@walmart.com']
        Contact result = Contact.repo.update(params)

        then:
        result != null

        when:
        //Checking updated contact
        Contact updatedContact = Contact.get(result.id)

        then:
        params.firstName == updatedContact.firstName
        params.email == updatedContact.email
        !contact.tagForReminders
    }

    def testSave_With_StringTrimmerEditor() {
        when:
        Map create = ['email': 'jbhasin@objectseek.com', firstName: 'DefContact', lastName: '  test']
        Contact contact = createEntity(create)

        then:
        'test' == contact.lastName
    }

    def testStringTrimmerEditor_forBlankSpaces(){
        when:
        def create = ['email':'jbhasin@objectseek.com', firstName:'DefContact', lastName:'     ']
        Contact contact = createEntity(create)

        then:
        contact.lastName == null
    }

    def testAssignUserNameFromContactName_noUser() {
        when:
        Contact contact = createEntity()
        contact.name = "Hello World"
        Contact updatedContact = Contact.repo.assignUserNameFromContactName(contact) // call method

        then:
        contact.name == "Hello World"
        !contact.user
        !updatedContact.user
    }

    def testAssignUserNameFromContactName_hasUser() {
        given:
        Contact contact = MockData.createContactWithUser()

        expect:
        contact != null
        contact.user != null
        contact.user?.name != contact.name

        when:
        contact.firstName = "John"
        contact.lastName = "Galt"
        contact.email = "galt@9ci.com"
        contact.validate()

        Contact result = Contact.repo.assignUserNameFromContactName(contact)

        then:
        result != null
        result.user.name == "John Galt"
        result.name == "John Galt"
    }

    def testCopy() {
        when:
        Contact old = createEntity()
        old.emails.clear()
        old.phones.clear()
        old.sources.clear()
        old.locations.clear()

        old.comments = "comments"
        old.addToPhones(TestData.build(ContactPhone,[contact: old]))
        old.addToEmails(TestData.build( ContactEmail,[contact: old]))
        old.addToSources(TestData.build( ContactSource,[contact: old]))
        old.persist(flush: true)
        def loc = TestData.build(Location, [id:1, org:old.org, contact: old]).persist()
        assert loc.id == 1
        assert loc.contact.id == old.id
        assert old.locations.size() == 1

        Contact copy = createEntity([org: old.org])
        Contact.repo.copy(old, copy)

        then:
        copy.name == old.name
        copy.num == old.num
        copy.email == old.email
        copy.firstName == old.firstName
        copy.nickName == old.nickName
        copy.comments == old.comments

        copy.phones.size() == 1
        copy.emails.size() == 1
        copy.sources.size() == 1
        copy.locations.size() == 1
    }

    def "test add and update locations"() {
        setup:
        Map params = buildCreateMap([:])
        params.locations = [[street1: "test street1"], [street1: "test street2"]]


        when: "adding location"
        def entity = Contact.create(params)
        flushAndClear()
        def contact = Contact.get(entity.id)

        then:
        contact.locations.size() == 2
        contact.locations[0].org == contact.org
        contact.locations[0].street1 == "test street1"
        contact.locations[1].street1 == "test street2"

    }

}
