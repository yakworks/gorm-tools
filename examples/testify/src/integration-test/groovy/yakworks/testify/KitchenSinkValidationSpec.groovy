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
import yakworks.gorm.testing.model.KitchenSinkExt

@Integration
@Rollback
class KitchenSinkValidationSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    def "test Org create"(){
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

    def "test Org create fail"(){
        when:
        Map invalidData2 = [num:'foo1', name: "foo", link: ["name": "", num:'foo2']]
        def sink = new KitchenSink()

        sink.bind(invalidData2)
        sink.validate()
        //Org.create(invalidData2)

        then:
        //def ex = thrown(EntityValidationException)
        sink.errors.errorCount == 3
        sink.errors['kind'].code == 'nullable'
        sink.errors['link.kind'].code == 'nullable'
        sink.errors['link.name'].code == 'nullable'
    }

    void "org and orgext validation success"(){
        when:
        def sink = new KitchenSink(num:'foo1', name: "foo", kind: KitchenSink.Kind.CLIENT)
        sink.ext = new KitchenSinkExt(kitchenSink: sink, textMax: 'fo')
        sink.persist()

        then:
        sink.id
        sink.ext.id
    }

    void "rejectValue only in LocationRepo beforeValidate"(){
        when:
        def sink = new KitchenSink(num:'foo1', name: "foo", kind: KitchenSink.Kind.CLIENT)
        sink.thing = new Thing(city: "AddyVille", country: 'USA')
        assert !sink.validate()
        //flushAndClear()

        then:
        //def ex = thrown(EntityValidationException)
        sink.errors.errorCount == 2
        //from repo
        sink.errors['location.city'].code == 'no.AddyVilles'
        //normal
        sink.errors['location.country'].code == 'maxSize.exceeded'
        //sink.location.errors['city'].code == 'no.AddyVilles'
    }

    void "org and orgext rejectValue in beforeValidate"(){
        when:
        def sink = new KitchenSink(num:'foo1', name: "foo", kind: KitchenSink.Kind.CLIENT)
        sink.thing = new Thing(city: "AddyVille", country: 'USA', street: 'RejectThis')
        sink.ext = new KitchenSinkExt(kitchenSink: sink, textMax: 'foo') //foo is 3 chars and should fail validation
        sink.persist()
        //flushAndClear()

        then:
        def ex = thrown(EntityValidationException)
        //normal validation errors
        sink.errors['ext.textMax'].code == 'maxSize.exceeded'
        sink.errors['thing.country'].code == 'maxSize.exceeded'
        //since its in orgRepo beforeValidate it shows up as nested
        sink.errors['thing.street'].code == 'no.from.KitchenSinkRepo'
        //comes from addy repo's beforeValidate
        sink.errors['thing.city'].code == 'no.from.ThingRepo'
    }

    def "test create validation fail"(){
        when:
        Map invalidData2 = [num:'foo1', name: "foo", link: ["name": "", num:'foo2']]
        KitchenSink.create(invalidData2)

        then:
        def ex = thrown(EntityValidationException)
        //its only 2 on this one as a default kind is set in the repo during create
        ex.errors.errorCount == 2
        ex.errors['link.kind']
        ex.errors['link.name'].code == 'nullable'
    }

}
