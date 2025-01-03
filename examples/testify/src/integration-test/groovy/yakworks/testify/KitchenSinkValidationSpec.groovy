package yakworks.testify


import gorm.tools.problem.ValidationProblem
import yakworks.testing.gorm.integration.SecuritySpecHelper
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt

@Integration
@Rollback
class KitchenSinkValidationSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    def "create"(){
        when:
        Long id = KitchenSink.create([num:'123', name:"Wyatt Oil"]).id
        flushAndClear()
        String[] beans = ctx.getBeanDefinitionNames()
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
        def sink = KitchenSink.repo.create(invalidData2, [persistAfterAction: false])

        sink.validate()
        //Org.create(invalidData2)

        then:
        //def ex = thrown(EntityValidationException)
        sink.errors.errorCount == 3
        sink.errors['kind'].code == 'NotNull'
        sink.errors['sinkLink.kind'].code == 'NotNull'
        sink.errors['sinkLink.name'].code == 'NotNull'
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
        sink.errors['thing.country'].code == 'MaxLength'
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
        def ex = thrown(ValidationProblem.Exception)
        ex.errors.errorCount == 4
        //normal validation errors
        sink.errors['ext.textMax'].code == 'MaxLength'
        sink.errors['thing.country'].code == 'MaxLength'
        sink.errors['thing.name'].code == 'no.from.ThingRepo'
        //comes from KitchenSinkRepo shows it allows any string in the field and code
        sink.errors['beatles'].code == 'no.backInThe.USSR.from.KitchenSinkRepo'
    }

    def "test create validation fail"(){
        when:
        Map invalidData2 = [num:'foo1', name: "foo", sinkLink: ["name": "", num:'foo2']]
        KitchenSink.create(invalidData2)

        then:
        def ex = thrown(ValidationProblem.Exception)
        //its only 2 on this one as a default kind is set in the repo during create
        ex.errors.errorCount == 2
        ex.errors['sinkLink.kind']
        ex.errors['sinkLink.name'].code == 'NotNull'
    }

}
