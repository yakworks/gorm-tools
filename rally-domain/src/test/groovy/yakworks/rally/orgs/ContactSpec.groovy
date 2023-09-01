package yakworks.rally.orgs

import gorm.tools.problem.ValidationProblem
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
import yakworks.security.gorm.model.AppUser
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.testing.gorm.unit.DataRepoTest

class ContactSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List<Class> entityClasses = [ Contact, AppUser, Org, OrgSource, OrgTypeSetup, Location, ContactPhone, ContactSource, ContactEmail]

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

    Contact createEntity(Map args = [:]){
        def orgId = build(Org).id
        args.putAll([orgId: orgId])
        def dta = buildMap(Contact, args)

        def entity = Contact.create(dta)
        //assert entity.properties == [foo:'foo']
        flushAndClear()

        return Contact.get(entity.id)
    }

    //show data table option 2
    def "test invalid email"() {
        when:
        MockData.contact(email: 'foo@bar.comx')
        // createEntity(email: 'foo@bar.comx')

        then:
        thrown(ValidationProblem.Exception)
    }

    def testEquals() {
        when:
        Contact contact1 = build(Contact)
        Contact contact2 = build(Contact)

        then:
        !contact1.equals(contact2)
        contact1 != contact2

    }

    def testHashCode() {
        when:
        Contact contact = build(Contact)
        int contactHash = contact.hashCode()

        def contact2 = build(Contact)
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
        def orgId = build(Org).id
        Map dta = [orgId: orgId, 'email': 'jbhasin@objectseek.com', firstName: 'DefContact', lastName: '  test']
        dta = buildMap(Contact, dta)
        Contact contact = Contact.create(dta)

        then:
        'test' == contact.lastName
    }

    def testStringTrimmerEditor_forBlankSpaces(){
        when:
        def orgId = build(Org).id
        def create = [orgId: orgId, 'email':'jbhasin@objectseek.com', firstName:'DefContact', lastName:'     ']
        Contact contact = Contact.create(buildMap(Contact, create))

        then:
        contact.lastName == null
    }

    def testAssignUserNameFromContactName_noUser() {
        when:
        Contact contact = build(Contact)
        contact.name = "Hello World"
        contact.email = "foo@bar.com"
        Contact.repo.syncChangesToUser(contact) // call method

        then:
        contact.name == "Hello World"
        !contact.user
    }

    def testAssignUserNameFromContactName_hasUser() {
        given:
        Contact contact = MockData.createContactWithUser()

        expect:
        contact != null
        contact.user != null
        contact.user?.name != contact.name

        when:
        contact.firstName = "jj"
        contact.lastName = "Galt"
        contact.email = "galt@9ci.com"
        contact.validate()

        Contact.repo.syncChangesToUser(contact)

        then:
        contact.user.name == "jj Galt"
        contact.name == "jj Galt"
        contact.user.email == "galt@9ci.com"
    }

    def testCopy() {
        when:
        Contact old = createEntity()
        old.emails.clear()
        old.phones.clear()
        old.locations.clear()

        old.comments = "comments"
        old.addToPhones(RepoTestData.build(ContactPhone,[contact: old]))
        old.addToEmails(RepoTestData.build( ContactEmail,[contact: old]))
        old.source = RepoTestData.build( ContactSource,[contactId: old.id])
        old.persist(flush: true)

        def loc = RepoTestData.build(Location, [org:old.org, contact: old]).persist(flush:true)
        assert loc.contact.id == old.id
        assert old.locations.size() == 1

        Contact copy = build(Contact, [org: old.org])
        Contact.repo.copy(old, copy)
        flush()

        then:
        copy.name == old.name
        copy.num == old.num
        copy.email == old.email
        copy.firstName == old.firstName
        copy.altName == old.altName
        copy.comments == old.comments

        copy.phones.size() == 1
        copy.emails.size() == 1
        //copy.source != null
        //copy.source.sourceId == old.source.sourceId
        copy.locations.size() == 1
    }

    def "test add and update locations"() {
        setup:
        def orgId = build(Org).id
        def params = buildMap(Contact)
        params.orgId = orgId
        params.locations = [[street1: "test street1"], [street1: "test street2"]]

        when: "adding location"
        def entity = Contact.create(params)
        flushAndClear()
        def contact = Contact.get(entity.id)

        then:
        contact.locations.size() == 2
        contact.locations[0].orgId == contact.orgId
        contact.locations[0].street1 == "test street1"
        contact.locations[1].street1 == "test street2"

    }

}
