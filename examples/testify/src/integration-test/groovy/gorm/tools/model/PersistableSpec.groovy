package gorm.tools.model

import yakworks.testing.gorm.integration.DataIntegrationTest
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

class PersistableSpec extends Specification implements DataIntegrationTest{

    def "Persistable check"() {
        when:
        Org orgNew = new Org()
        Persistable persistable = orgNew as Persistable

        then: "should be instance of Persistable and isNew"
        !orgNew.version
        orgNew instanceof Persistable
        persistable.isNew()
        orgNew.isNew()

    }

}
