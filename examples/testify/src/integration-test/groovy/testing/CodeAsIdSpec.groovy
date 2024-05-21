package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.ThingStringId

@Integration
@Rollback
class CodeAsIdSpec extends Specification implements DomainIntTest {

    void "sanity check"() {
        expect:
        ThingStringId.repo
    }

    void "save and update"() {
        when:
        ThingStringId thing = new ThingStringId(name:"test")
        thing.id = "t1"
        thing.persist(flush:true)

        then:
        noExceptionThrown()

        when:
        thing = ThingStringId.get("t1")

        then:
        thing.name == "test"
        thing.code == "t1"
        ThingStringId.findWhere(code:"t1")

        when: "update"
        ThingStringId.update(id:"t1", name:"test-updated")
        flush()

        then:
        ThingStringId.findWhere(code:"t1").name == "test-updated"
    }

}
