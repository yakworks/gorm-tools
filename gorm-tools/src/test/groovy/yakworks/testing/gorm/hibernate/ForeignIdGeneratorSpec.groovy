/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.hibernate

import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import spock.lang.Specification
import yakworks.testing.gorm.unit.GormHibernateTest

class ForeignIdGeneratorSpec extends Specification implements GormHibernateTest {

    static List entityClasses = [FidMaster, FidChild]

    def "test create"() {
        when:
        Long firstId
        (1..5).each {
            def master = new FidMaster(name: 'bob')
            //master.id = 1
            master.child = new FidChild(name: 'foo')
            master.persist()
            firstId = master.id
        }
        flushAndClear()

        then:
        def mt = FidMaster.get(firstId)
        mt.name == "bob"
        mt.child.name == 'foo'

    }

    def "test assigned id"() {
        when:
        def master = new FidMaster(name: 'bob1234')
        master.id = 1234
        master.child = new FidChild(name: 'foo1234')
        master.persist()
        flushAndClear()

        then:
        def mt = FidMaster.get(1234)
        mt.name == "bob1234"
        mt.child.name == 'foo1234'
    }

    def "test validation error on child"() {
        when:
        def master = new FidMaster(name:'bob')
        //master.id = 1
        master.child = new FidChild()
        master.persist()
        //flushAndClear()

        then: "should get an error for child.name field"
        def e = thrown(ValidationProblem.Exception)
        def problem = e.problem
        problem.entity == master
        problem.errors.objectName == "yakworks.testing.gorm.hibernate.FidMaster"
        problem.errors.allErrors.size() == 1
        def fe = problem.errors.getFieldError("child.name")
        fe.code == 'NotNull'
        fe.rejectedValue == null
        // fe.arguments as List == ['name', FidChild]
    }

}

@Entity @GrailsCompileStatic
class FidMaster implements RepoEntity<FidMaster> {
    String name

    FidChild child

    static mapping = {
        id generator:'gorm.tools.hibernate.SpringBeanIdGenerator', params:[foo:'bar']
    }

    static constraints = {
        name nullable: false
    }
}

@Entity @GrailsCompileStatic
class FidChild implements RepoEntity<FidChild>{
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
