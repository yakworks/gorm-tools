package yakworks.rally.util

import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import yakworks.rally.orgs.model.Contact
import gorm.tools.hibernate.criteria.CriteriaUtils
import spock.lang.Specification

@Integration
@Rollback
class CriteriaUtilsSpec extends Specification implements DataIntegrationTest {

    def testOrder_oneColumn() {

        when:
        def crit = Contact.createCriteria()
        def res = crit.list(){
            ge 'id', 0L
            CriteriaUtils.applyOrder([sort: "email", order: "desc"], delegate)
        }

        def res2 = Contact.createCriteria().list(){
            ge 'id', 0L
            order("email", "desc")
        }

        then:
        res.eachWithIndex{ def entry, int i ->
            assert entry.email == res2[i].email
        }
    }

    def testOrder_twoColumns() {

        when:
        def crit = Contact.createCriteria()
        def res = crit.list(){
            ge 'id', 0L
            CriteriaUtils.applyOrder([sort: "email asc, firstName", order: "desc"], delegate)
        }

        def res2 = Contact.createCriteria().list(){
            ge 'id', 0L
            and {
                order("email", "asc")
                order("firstName", "desc")
            }
        }

        then:
        res.eachWithIndex{ def entry, int i ->
            assert entry.email == res2[i].email
        }
    }

    def testOrder_withNested() {

        when:
        def crit = Contact.createCriteria()
        def res = crit.list(){
            ge 'id', 0L
            CriteriaUtils.applyOrder([sort: "org.id asc, firstName", order: "desc"], delegate)
        }

        def res2 = Contact.createCriteria().list(){
            ge 'id', 0L
            and {
                org {
                    order("id", "asc")
                }
                order("firstName", "desc")
            }
        }

        then:
        res.eachWithIndex{ def entry, int i ->
            assert entry.email == res2[i].email
        }
    }

}
