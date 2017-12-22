package testing.specs

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.testing.GormToolsHibernateSpec
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

class ForeignIdGeneratorSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [FidMaster, FidChild] }

    def "test create"() {
        when:
        def master = new FidMaster(name:'bob')
        master.id = 1
        master.child = new FidChild(name:'foo')
        master.persist()
        flushAndClear()

        then:
        def mt = FidMaster.get(1)
        mt.name == "bob"
        mt.child.name == 'foo'

    }

    def "test validation error on child"() {
        when:
        def master = new FidMaster(name:'bob')
        master.id = 1
        master.child = new FidChild()
        master.persist()
        //flushAndClear()

        then: "should get an error for child.name field"
        def e = thrown(EntityValidationException)
        e.entity == master
        e.cause.class == grails.validation.ValidationException
        e.errors.objectName == "testing.specs.FidMaster"
        e.errors.allErrors.size() == 1
        def fe = e.errors.getFieldError("child.name")
        fe.code == 'nullable'
        fe.rejectedValue == null
        fe.arguments as List == ['name', testing.specs.FidChild]
    }

}

@Entity @GrailsCompileStatic
class FidMaster {
    String name

    FidChild child

    static mapping = {
        id generator:'assigned'
    }

    static constraints = {
        name nullable: false
    }
}

@Entity @GrailsCompileStatic
class FidChild {
    static belongsTo = [master:FidMaster]

    String name

    static mapping = {
        id generator:'foreign', params:[property:'master']
        master insertable: false, updateable: false , column:'id'
    }
    static constraints = {
        name nullable: false
    }
}
