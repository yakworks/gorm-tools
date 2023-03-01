package yakworks

import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class OrgSpec extends Specification implements DataIntegrationTest {

    def "sanity check for Org create outside of plugin"(){
        when:
        Long id = Org.create([num:'123', name:"Wyatt Oil", type:"Customer"]).id
        flushAndClear()

        then:
        def c = Org.get(id)
        c.name == 'Wyatt Oil'
    }
}
