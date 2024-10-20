package yakworks.rally.activity

import yakworks.json.groovy.JsonEngine
import yakworks.testing.gorm.integration.SecuritySpecHelper
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.orgs.model.Contact

/**
 * test the data ops that come in from rest
 * using attachment with tags to test adding and removing links
 */
@Integration
@Rollback
class ActivityContactOpTests extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    List<Contact> createSomeContacts(){
        List items = []
        (0..9).each { eid ->
            items <<  Contact.create(firstName: "Name$eid", org:[id: 1], sourceId:"Test$eid")
        }
        flushAndClear()
        return items
    }

    Activity setupData(List contacts = null){
        if(!contacts) contacts = createSomeContacts()
        List<Map> cmap = [
            [id:contacts[0].id],
            [id:contacts[1].id],
            [id:contacts[2].id]
        ]

        flush()

        def data = [
            org    : [id: 1],
            note   : [body: 'test note'],
            contacts: cmap
        ]

        def a = Activity.create(data)
        flush()
        assert a.contacts.size() == 3
        return a
    }

    void "sanity check"() {
        when:
        def att = setupData()
        flush() //needed for the checks as it queries

        then:
        ActivityContact.list(att).size() == 3
    }

    void "replace is the default if nothing specified"() {
        when:
        def cons = createSomeContacts()
        def att = setupData(cons)

        def id = att.id
        def dta = [
            id: att.id,
            contacts: [
                [id: cons[3].id],
                [id: cons[4].id]
            ]
        ]
        def updatedAtt = Activity.repo.update(dta)
        flush()
        then:
        updatedAtt.contacts.size() == 2

    }

    void "if a contact already exists then will simply keep it and not add it"() {
        when:
        def tags = createSomeContacts()
        def att = setupData(tags)

        def id = att.id
        def dta = [
            id: att.id,
            contacts: [
                [id: tags[0].id], //already exists
                [id: tags[3].id],
                [id: tags[4].id]
            ]
        ]
        def updatedAtt = Activity.repo.update(dta)
        flush()
        then:
        updatedAtt.contacts.size() == 3

    }

    void "test op:update with empty array does nothing"() {
        when:
        assert ActivityContact.list().isEmpty()
        def att = setupData()
        flushAndClear()

        def id = att.id
        def dta = [
            id: att.id,
            contacts: [
                op:'update', data: []
            ]
        ]
        def updatedAtt = Activity.repo.update(dta)
        flush()

        then:
        ActivityContact.list(updatedAtt).size() == 3
    }

    void "test op:replace with empty array removes them"() {
        when:
        assert ActivityContact.list().isEmpty()
        def att = setupData()
        flushAndClear()

        def id = att.id
        def dta = [
            id: att.id,
            contacts: []
        ]
        def updatedAtt = Activity.repo.update(dta)
        flush()

        then:
        ActivityContact.list(updatedAtt).size() == 0
    }

    void "test op:remove on one"() {
        when:
        def att = setupData()
        def id1 = att.contacts[0].id
        assert id1
        flushAndClear()

        def id = att.id
        def dta = [
            id: att.id,
            contacts: [
                op:'update',
                data: [
                    [ op:'remove', id: id1 ]
                ]
            ]
        ]
        def updatedAtt = Activity.repo.update(dta)

        then:
        updatedAtt.contacts.size() == 2

    }

    void "test op:update replace"() {
        when:
        def tags = createSomeContacts()
        def att = setupData(tags)

        def id = att.id
        def dta = [
            id  : att.id,
            contacts: [
                op  : 'update',
                data: [
                    [id: tags[0].id], //this was already here and should remain
                    [id: tags[1].id], //this was already here and should remain
                    [id: tags[3].id],
                    [id: tags[4].id],
                ]
                //so the above data only adds 2, there is 1 that exists not mentioned and it will remain
            ]
        ]
        def updatedAtt = Activity.repo.update(dta)
        flush()
        def contacts = updatedAtt.contacts

        then: "it kept the 2 existing and added 2 to the 3 that existed"

        contacts.size() == 5
        contacts[0].name == 'Name0'
        contacts[1].name == 'Name1'
        contacts[2].name == 'Name2' //still here even though not refed above
        contacts[3].name == 'Name3'
        contacts[4].name == 'Name4'
    }

    void "op:update and remove with lookup"() {
        setup:
        List<Contact> contacts = createSomeContacts()
        Activity activity = setupData(contacts)

        when:
        def dta = [
            id      : activity.id,
            contacts: [
                op  : 'update',
                data: [
                    [sourceId: "Test3"], //should lookup contact from sourceid
                    [sourceId: "Test4"],
                ]
            ]
        ]

        def updatedAtt = Activity.update(dta)
        flush()
        def updatedContacts = updatedAtt.contacts

        then: "should add 2 new contacts, should lookup contacts by sourceid"
        updatedContacts.size() == 5
        updatedContacts[3].id == contacts[3].id
        updatedContacts[4].id == contacts[4].id

        when: "op:remove with lookup"
        dta = [
            id      : activity.id,
            contacts: [
                op  : 'remove',
                data: [
                    [sourceId: "Test1"],//should lookup contact1 and 2 from sourceid and remove
                    [sourceId: "Test2"],
                ]
            ]
        ]

        updatedAtt = Activity.update(dta)
        flush()
        updatedContacts = updatedAtt.contacts

        then: "should add 2 new contacts, should lookup contacts by sourceid"
        updatedContacts.size() == 3
    }

    void "replace with lookup"() {
        when:
        def cons = createSomeContacts()
        def att = setupData(cons)

        def dta = [
            id: att.id,
            contacts: [
                [sourceId: "Test3"], //should look up by sourceid
                [sourceId: "Test4"],
            ]
        ]

        def updatedAtt = Activity.update(dta)
        flush()

        then:
        updatedAtt.contacts.size() == 2
        updatedAtt.contacts[0].id == cons[3].id
        updatedAtt.contacts[1].id == cons[4].id
    }

    void "test update with json string"() {
        setup:
        List<Contact> contacts = createSomeContacts()
        Activity activity = setupData(contacts)

        when: "data contains ids"
        def dta = [
            id  : activity.id,
            contacts: """[{"id": "${contacts[3].id}"}, {"id": "${contacts[4].id}"}]""".toString()
        ]
        def updatedAtt = Activity.update(dta)
        flush()
        def updatedContacts = updatedAtt.contacts

        then:
        updatedContacts.size() == 2
        updatedContacts[0].id == contacts[3].id
        updatedContacts[1].id == contacts[4].id
    }

    void "test update with json :  lookup"() {
        setup:
        List<Contact> contacts = createSomeContacts()
        Activity activity = setupData(contacts)

        when: "data contains ids"
        def dta = [
            id  : activity.id,
            contacts: """[{"sourceId": "Test3"}, {"sourceId": "Test4"}]"""
        ]

        def updatedAtt = Activity.update(dta)
        flush()
        def updatedContacts = updatedAtt.contacts

        then:
        updatedContacts.size() == 2
        updatedContacts[0].id == contacts[3].id
        updatedContacts[1].id == contacts[4].id
    }

}
