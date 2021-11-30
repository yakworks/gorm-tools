package yakworks.rally.orgs


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class OrgMangoSumTests extends Specification implements DomainIntTest {

    def "sum simple"() {
        when:
        def qry = Org.query {
            projections {
                sum('id')
                groupProperty('type')
            }
        }
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 5
        sumbObj[0][1] == OrgType.Customer
    }

    // start of how to do sums on deep associations
    def "sum association"() {
        when:
        def qry = Org.query {
            createAlias('contact', 'contact')
            createAlias('contact.flex', 'contact_flex')
            projections {
                sum('contact_flex.num1')
            }
        }
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 1
    }

}
