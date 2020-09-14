package yakworks.taskify.domain

import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class ContactCrudSpec extends Specification implements DataIntegrationTest {

    def "test Contact create"(){
        when:
        Long id = Contact.create([name:"John Galt"]).id
        flushAndClear()

        then:
        def c = Contact.get(id)
        c.firstName == 'John'
    }
}
