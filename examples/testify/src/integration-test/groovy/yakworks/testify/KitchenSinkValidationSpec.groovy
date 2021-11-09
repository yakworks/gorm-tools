package yakworks.testify

import gorm.tools.beans.AppCtx
import gorm.tools.repository.errors.EntityValidationException
import yakworks.gorm.testing.SecuritySpecHelper
import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.model.Thing
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt

@Integration
@Rollback
class KitchenSinkValidationSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    def "create"(){
        when:
        Long id = KitchenSink.create([num:'123', name:"Wyatt Oil"]).id
        flushAndClear()
        String[] beans = AppCtx.ctx.getBeanDefinitionNames()
        for (String bean : beans) {
            println(bean)
        }

        then:
        def c = KitchenSink.get(id)
        c.num == '123'
        c.name == 'Wyatt Oil'
        c.createdDate
        c.createdBy == 1
        c.editedDate
        c.editedBy == 1
    }

    def "create fail"(){
        when:
        Map invalidData2 = [num:'foo1', name: "foo", sinkLink: ["name": "", num:'foo2']]
        def sink = new KitchenSink()

        sink.bind(invalidData2)
        sink.validate()
        //Org.create(invalidData2)

        then:
        //def ex = thrown(EntityValidationException)
        sink.errors.errorCount == 3
        sink.errors['kind'].code == 'nullable'
        sink.errors['sinkLink.kind'].code == 'nullable'
        sink.errors['sinkLink.name'].code == 'nullable'
    }

    void "validation success"(){
        when:
        def sink = new KitchenSink(num:'foo1', name: "foo", kind: KitchenSink.Kind.CLIENT)
        sink.ext = new SinkExt(kitchenSink: sink, name: "foo", textMax: 'fo')
        sink.persist()

        then:
        sink.id
        sink.ext.id
    }

    void "rejectValue only in ThingRepo beforeValidate"(){
        when:
        def sink = new KitchenSink(num:'foo1', name: "foo", kind: KitchenSink.Kind.CLIENT)
        sink.thing = new Thing(name: "RejectThis", country: 'USA')
        assert !sink.validate()
        //flushAndClear()

        then:
        //def ex = thrown(EntityValidationException)
        sink.errors.errorCount == 2
        //from repo
        sink.errors['thing.name'].code == 'no.from.ThingRepo'
        //normal
        sink.errors['thing.country'].code == 'maxSize.exceeded'
        //sink.location.errors['city'].code == 'no.AddyVilles'
    }

    void "rejectValue in beforeValidate on many repos"(){
        when:
        def sink = new KitchenSink(num:'foo1', name: "foo", kind: KitchenSink.Kind.CLIENT)
        sink.thing = new Thing(name: "RejectThis", country: 'USSR')
        sink.ext = new SinkExt(kitchenSink: sink, textMax: 'foo', name: "ext2") //foo is 3 chars and should fail validation
        sink.persist()
        //flushAndClear()

        then:
        def ex = thrown(EntityValidationException)
        ex.errors.errorCount == 4
        //normal validation errors
        sink.errors['ext.textMax'].code == 'maxSize.exceeded'
        sink.errors['thing.country'].code == 'maxSize.exceeded'
        sink.errors['thing.name'].code == 'no.from.ThingRepo'
        //comes from KitchenSinkRepo show sit can be anythings
        sink.errors['beatles'].code == 'no.backInThe.USSR.from.KitchenSinkRepo'
    }

    def "test create validation fail"(){
        when:
        Map invalidData2 = [num:'foo1', name: "foo", sinkLink: ["name": "", num:'foo2']]
        KitchenSink.create(invalidData2)

        then:
        def ex = thrown(EntityValidationException)
        //its only 2 on this one as a default kind is set in the repo during create
        ex.errors.errorCount == 2
        ex.errors['sinkLink.kind']
        ex.errors['sinkLink.name'].code == 'nullable'
    }

}
