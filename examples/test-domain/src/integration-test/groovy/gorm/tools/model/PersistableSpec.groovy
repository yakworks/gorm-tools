package gorm.tools.model

import gorm.tools.testing.integration.DataIntegrationTest
import spock.lang.Specification
import yakworks.taskify.domain.Org

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
