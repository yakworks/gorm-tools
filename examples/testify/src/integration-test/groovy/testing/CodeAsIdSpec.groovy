package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemException
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
        ThingStringId thing = new ThingStringId(name:"test", code:"t1")
        thing.persist(flush:true)

        then:
        noExceptionThrown()

        when:
        thing = ThingStringId.get("t1")

        then:
        thing.name == "test"
        thing.code == "t1"
        ThingStringId.findWhere(code:"t1")

        and: "query"
        ThingStringId.query("id":"t1").get()
        ThingStringId.query("code":"t1").get()

        when: "update"
        ThingStringId.repo.update(id:"t1", name:"test-updated")
        flush()

        then:
        ThingStringId.findWhere(code:"t1").name == "test-updated"
    }

    void "test duplicate code"() {
        when:
        ThingStringId thing = new ThingStringId(name:"test", code:"t1")
        thing.persist()
        flushAndClear()

        ThingStringId thing2 = new ThingStringId(name:"test2", code:"t1")
        thing2.persist(flush:true)

        then:
        DataProblemException ex = thrown()
        ex.detail.contains "Unique index or primary key violation"
    }

}
