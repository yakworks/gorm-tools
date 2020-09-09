package yakworks.taskify.domain

import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification
import gorm.tools.security.testing.SecuritySpecHelper

@Integration
@Rollback
class OrgCrudSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    def "test Contact create"(){
        when:
        Long id = Org.create([num:'123', name:"Wyatt Oil"]).id
        flushAndClear()

        then:
        def c = Org.get(id)
        c.num == '123'
        c.name == 'Wyatt Oil'
        c.createdDate
        c.createdBy == 1
        c.editedDate
        c.editedBy == 1

    }
}
