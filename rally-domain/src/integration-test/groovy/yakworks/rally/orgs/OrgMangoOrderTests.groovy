package yakworks.rally.orgs

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class OrgMangoOrderTests extends Specification implements DomainIntTest {


    @Ignore //XXX this should work when we add a manual alias, seems to be getting confused with the auto ones?
    def "order query the manual way"() {
        when:
        def qry = Org.query {
            createAlias('contact', 'contact')
            createAlias('contact.flex', 'contact_flex')
            order "contact_flex.num1", 'desc'
        }
        def list = qry.list()
        then:
        list[0].contact.flex.num1 > list[1].contact.flex.num1
    }

    def "order when alias also has a search"() {
        when:
        def detachedQuery = Org.query{
            contact {
                flex {
                    gt "num1", 5
                }
            }
        }
        detachedQuery.order('contact.flex.num1','desc')
        //just here to sanity check it works
        def hibernateQuery = detachedQuery.getHibernateQuery()

        // hibernateQuery.order(new Query.Order('contact.flex.num1', Query.Order.Direction.DESC))
        def list = detachedQuery.list()
        then:
        list[0].contact.flex.num1 > list[1].contact.flex.num1
    }


    def "order query method"() {
        when:
        def qry = Org.query{}
        qry.order('contact.flex.num1','desc')
        def list = qry.list()
        then:
        list[0].contact.flex.num1 > list[1].contact.flex.num1
    }

    def "order query with mango desc"() {
        when:
        def qry = Org.query('$sort':["contact.flex.num1": 'desc'])
        def list = qry.list()
        then:
        list[0].contact.flex.num1 > list[1].contact.flex.num1
    }


    def "order query with mango asc"() {
        when:
        def qry = Org.query('$sort':["contact.flex.num1": 'asc'])
        def list = qry.list()
        then:
        list[0].contact.flex.num1 < list[1].contact.flex.num1
    }

    def "order just flex as a sanity check"() {
        when:
        def qry = Contact.query {
            // createAlias('flex', 'flex')
            order "flex.num1", 'desc'
        }

        def list = qry.list()

        then:
        list[0].flex.num1 > list[1].flex.num1
    }


}
