package yakworks.taskify.domain

import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
class OrgSpec extends Specification implements DataIntegrationTest {

    def "test Org create"(){
        when:
        Long id = Org.create([num:'123', name:"Wyatt Oil"]).id
        flushAndClear()

        then:
        def c = Org.get(id)
        c.name == 'Wyatt Oil'
    }
}
