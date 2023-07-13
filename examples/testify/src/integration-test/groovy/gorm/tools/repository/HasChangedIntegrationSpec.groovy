package gorm.tools.repository

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.Thing

/**
 * COPY OF HasChangedSpec, sanity check same thing with integration tests
 */
@Integration
@Rollback
class HasChangedIntegrationSpec extends Specification implements DomainIntTest {

    def setupSpec() {
        KitchenSinkRepo.doTestAuditStamp = false
    }

    void "Org smoke test"() {
        when:
        Org org = new Org(num:"o-1", name:"o-1", companyId: 2, type: OrgType.Customer).persist()
        org.persist()

        then:
        !org.hasChanged("num")
        !org.hasChanged()

    }

    void "hasChanged on new"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name")

        then: "new objects show up as changed"
        sink.hasChanged()
        sink.hasChanged("num")

        and: "but show even if it didnt actually get populated, so its not accurate on fields"
        sink.hasChanged("name2")
    }

    void "hasChanged change persisted"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()

        when:
        //change trait field
        sink.num = "456"
        //change KitchenSink field
        sink.name2 = "foo"

        then:
        sink.hasChanged("num")
        sink.hasChanged("name2")
        sink.hasChanged()

    }

    void "markDirty works"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()
        sink.markDirty()

        then:
        sink.hasChanged()

        when:
        sink.num = "456"

        then:
        sink.hasChanged("num")
        sink.hasChanged()
    }

    void "new instance is dirty till saved"() {
        when: "nothing changed"
        KitchenSink sink = new KitchenSink()

        then:
        sink.hasChanged() //this is because DirtyCheckable.hasChanged returns true if `$changedProperties` is null
        sink.isDirty() //behavior differs from hibernate
    }

    void "new instance is not dirty after save without flush"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        //this is because `ClosureEventTriggeringInterceptor.activateDirtyChecking` gets called after hibernate saveOrUpdate event
        //which checks if $changedProperties is null (it is null for new instance) then sets the $changedProperties to empty list
        //saveOrUpdate event fires even if there's no flush
        !sink.hasChanged()
    }

    void "PROBLEM - remains dirty untill flush"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()

        when:
        sink.name = "name2"

        then:
        sink.hasChanged()

        when: "persist should reset dirty status"
        sink.persist()

        then: "not dirty after save/persist"
        //XXX remains dirty untill we flush. Its same behavior as hibernate's isDirty
        //This is because in this case ClosureEventTriggeringInterceptor.activateDirtyChecking does not reset dirty status
        //because $changedProperties is not null.
        //And dirty status gets reset only after flush by `GrailsEntityDirtinessStrategy.resetDirty`
        //Hibernate supports `CustomEntityDirtinessStrategy` grails implementation is `GrailsEntityDirtinessStrategy`
        !sink.hasChanged('name')
        !sink.hasChanged()
    }

    void "hasChanged change persist called twice or hasChanged called twice fails"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        //this will trigger auditstamp in repo and update the edited time.
        sink.persist(flush:true)

        then:
        //this is fine
        !sink.hasChanged("num")
        //this is not
        !sink.hasChanged()

        when:
        sink.num = "456"

        then:
        //why does this fail now?
        sink.hasChanged("num")
        sink.hasChanged()

    }

    void "hasChanged with association with belongsto"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("ext")

        when:
        sink.ext = new SinkExt(kitchenSink: sink, name: "foo", textMax: 'fo')

        then:
        sink.hasChanged()
        sink.hasChanged("ext")

        when: "changes made to ext assoc"
        sink.persist(flush: true) //see comment below
        assert !sink.hasChanged()
        assert !sink.ext.hasChanged()
        sink.ext.name = "foo2"

        then: "ext shows changes"
        sink.ext.hasChanged()
        sink.ext.hasChanged("name")

        and: "the main sink does not show so it does not cascadem which is obvious when looking at source"
        !sink.hasChanged()
    }

    void "hasChanged with free association"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("thing")

        when:
        sink.thing = new Thing(name: "thing1")

        then:
        sink.hasChanged()
        sink.hasChanged("thing")
        sink.thing.hasChanged()
    }

    void "hasChanged with saved association"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("thing")

        when:
        sink.thing = new Thing(name: "thing1").persist()

        then:
        sink.hasChanged()
        sink.hasChanged("thing")
        !sink.thing.hasChanged()

        when: "changes made to thing assoc"
        sink.persist(flush: true) //see comment below
        assert !sink.hasChanged()
        sink.thing.name = "thing2"

        then:
        sink.thing.hasChanged()
        sink.thing.hasChanged("name")
        //sink does not show changes unlike with belongsTo
        !sink.hasChanged()
    }



    void "hasChanged with hasMany"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("stringList")

        when:
        sink.stringList = ['foo', 'bar']

        then:
        sink.hasChanged()
        sink.hasChanged("stringList")
    }

    void "hasChanged when same value is set"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()

        when:
        sink.num = "123"

        then:
        !sink.hasChanged()
        !sink.hasChanged("num")

        when:
        sink.num = "456"

        then:
        sink.hasChanged()
        sink.hasChanged("num")

        when:
        //XXX same as above, only works with flush
        // sink.persist()
        //it seems to work with flush but why, thats not consistent?
        sink.persist(flush: true)

        then:
        //HERE BE DRAGONS, why is this failing without flush:true? its like the second persist doesnt clear? or is it just spock?
        // maybe put in normal class and see if its does the same thing or must if be flushed to be accurate? which is kind of bad.
        !sink.hasChanged()
        !sink.hasChanged("num")

        when:
        sink.num = "456"

        then:
        !sink.hasChanged()
        !sink.hasChanged("num")

    }
}
