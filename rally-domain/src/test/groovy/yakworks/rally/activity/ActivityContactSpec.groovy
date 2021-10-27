package yakworks.rally.activity

import gorm.tools.repository.model.CriteriaRemover
import gorm.tools.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import grails.gorm.DetachedCriteria
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.repo.ActivityContactRepo
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org

class ActivityContactSpec extends Specification implements DomainRepoTest<ActivityContact>, SecurityTest {

    @Shared
    ActivityContactRepo repo

    void setupSpec() {
        mockDomains(Activity, ActivityNote, Contact, Org)
        repo = ActivityContact.repo
        repo.criteriaRemover = new CriteriaRemover(){
            void deleteAll(DetachedCriteria crit) {
                crit.list()*.delete()
            }
        }
    }

    List createSomeContacts(Long orgId){
        List items = []
        (0..9).each { eid ->
            items <<  Contact.create(firstName: "Name$eid", org:[id: orgId])
        }
        flushAndClear()
        return items
    }

    Activity setupData(List contacts = null){
        def org = build(Org)
        if(!contacts) contacts = createSomeContacts(org.id)
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


    void "check add and delete methods"() {
        when:
        def org = build(Org)
        def c = createSomeContacts(org.id)
        def con1 = c[0]
        def con2 = c[1]
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

        then:
        !repo.exists(act, con2)
        repo.count(act) == 2

        when: 'remove all by org'
        repo.remove(act)

        then:
        repo.count(act) == 0
    }

}
