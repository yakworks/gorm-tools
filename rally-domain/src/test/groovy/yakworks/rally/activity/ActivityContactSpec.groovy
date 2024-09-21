package yakworks.rally.activity

import gorm.tools.repository.model.CriteriaRemover
import grails.gorm.DetachedCriteria
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.repo.ActivityContactRepo
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class ActivityContactSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Activity, ActivityContact, ActivityNote, Contact, ContactSource, Org, Tag]

    @Shared
    ActivityContactRepo repo

    void setupSpec() {
        //setup remover
        repo = ActivityContact.repo
        repo.criteriaRemover = new CriteriaRemover(){
            void deleteAll(DetachedCriteria crit) {
                crit.list()*.delete()
            }
        }
    }

    List createSomeContacts(Long orgId){
        List items = []
        (0..9).eachWithIndex { eid, idx ->
            items <<  Contact.repo.create([id: idx+1, firstName: "Name$eid", org:[id: orgId]],[bindId:true])
        }
        flushAndClear()
        return items
    }

    Activity setupData(List contacts){
        // def org = build(Org)
        // if(!contacts) contacts = createSomeContacts(org.id)
        List<Map> cmap = [
            [id:contacts[0].id],
            [id:contacts[1].id],
            [id:contacts[2].id]
        ]

        flush()

        def data = [
            org    : contacts[0].org,
            note   : [body: 'test note'],
            contacts: cmap
        ]

        def a = Activity.create(data)
        flush()
        assert a.contacts.size() == 3
        return a
    }

    void "check add and delete methods"() {
        when:
        def org = build(Org)

        List c = createSomeContacts(org.id)
        Contact con1 = c[0]
        Contact con2 = c[1]
        def act = setupData(c)
        flush() //needed for the checks as it queries

        then: 'everything OK'
        act

        //exists
        repo.count(act) == 3
        repo.count(con1) == 1
        repo.exists(act, con1)
        repo.exists(act, con2)

        //lists work
        repo.list(act).size() == 3
        repo.list(con1).size() == 1

        when: 'remove 1 by key'
        repo.remove(act, con2)
        flush()
        then:
        !repo.exists(act, con2)
        repo.count(act) == 2

        when: 'remove all by org'
        repo.remove(act)
        flush()

        then:
        repo.count(act) == 0
    }

}
