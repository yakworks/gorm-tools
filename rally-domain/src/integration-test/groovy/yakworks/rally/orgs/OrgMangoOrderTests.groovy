package yakworks.rally.orgs

import java.time.LocalDate

import org.springframework.dao.DataRetrievalFailureException

import gorm.tools.model.SourceType
import gorm.tools.problem.ValidationProblem
import gorm.tools.testing.TestDataJson
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.problem.data.DataProblemCodes
import yakworks.problem.data.DataProblemException
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.testing.MockData

@Integration
@Rollback
class OrgMangoOrderTests extends Specification implements DomainIntTest {

    def "order query"() {
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

    def "order query mango"() {
        when:
        def qry = Org.query('$sort':["contact.flex.num1": 'desc'])
        def list = qry.list()
        then:
        list[0].contact.flex.num1 > list[1].contact.flex.num1
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
