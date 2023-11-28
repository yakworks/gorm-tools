package yakworks.rally.orgs

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.DataProblemException
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.repo.ContactSourceRepo
import yakworks.testing.gorm.integration.DataIntegrationTest

@Integration
@Rollback
class ContactSourceRepoTests extends Specification implements DataIntegrationTest {

    ContactSourceRepo contactSourceRepo

    void "test exists"() {
        given:
        Contact contact = Contact.findByNum("secondary2")

        expect:
        contact

        when:
        ContactSource source = ContactSource.create(source:"source123", sourceId: "123", contactId: contact.id)
        flush()

        then:
        source

        and:
        contactSourceRepo.exists("123")
        !contactSourceRepo.exists("1234")

        when: "try to create dupe"
        ContactSource.create(source:"source1234", sourceId: "123", contactId: contact.id)

        then:
        DataProblemException ex = thrown()
        ex.code == DataProblemCodes.UniqueConstraint.code
        ex.detail.contains "Violates unique constraint"
    }
}
