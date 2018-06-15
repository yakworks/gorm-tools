package testing

import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.validation.ValidationException
import org.springframework.dao.DataAccessException
import org.springframework.dao.OptimisticLockingFailureException
import spock.lang.Specification

//tests the persist and remove methods
@Integration
@Rollback
class DomainMethodsTests extends Specification {
    static dataInit = false

    void setup() {
        initData()
    }

    void initData() {
        (1..10).each {
            def jumper = new Jumper(name: "jumper$it")
            //jumper.student = new Student(name:"student$it")
            assert jumper.persist()
            def stud = new Student(name: "student$it")
            stud.jumper = jumper
            assert stud.persist()
        }
        //assert dom.persist()
        assert Jumper.count() == 10
        assert Student.count() == 10
        dataInit = true
    }

    void testPersist() {
        setup:
        initData()
        when:
        def check = Jumper.findByName("jumper1")
        then:
        check.name == "jumper1"
    }

    void testRemove() {
        setup:
        initData()
        when:
        def dom = Student.findByName("student1")
        assert dom.name == "student1"
        dom.remove()
        RepoUtil.flushAndClear()
        then:
        Student.findByName("student1") == null
    }

    void testPersistArgs() {
        when:
        assert new Jumper(name: "jumpargs").persist(flush: true)

        def check = Jumper.findByName("jumpargs")
        then:
        assert check.name == "jumpargs"
    }

    void testPersistFailValidation() {
        when:
        def jump = new Jumper()
        then:
        try {
            jump.persist()
            fail "it was supposed to fail the save because of validationException"
        } catch (EntityValidationException e) {
            assert e.cause instanceof grails.validation.ValidationException
            assert e.entity == jump
        }
    }

    void testPersistFailDataAccess() {
        setup:
        initData()
        when:
        def jump = Jumper.first()
        then:
        try {
            Jumper.executeUpdate("update Jumper j set j.version=20 where j.name='jumper1'")
            jump.name = 'fukt'
            jump.persist(flush: true)
            fail "it was supposed to fail the save because of OptimisticLockingFailureException"
        } catch (OptimisticLockingFailureException e) {
            assert e.message == "Another user has updated the testing.Jumper while you were editing"
        }
    }

    /*void testRemoveFail(){
        setup:
        initData()
        when:
        def jump = Jumper.findByName("jumper1")
        assert jump
        then:
        try{
            jump.remove()
            fail "it was supposed to fail because of a foreign key constraint"
        }catch(e){
            e.entity == jump
        }
    }*/

    void testInsert() {

        expect:
        def jumper = Jumper.create([name: "testInsert"])
        assert jumper
        RepoUtil.flushAndClear()
        def dom2 = Jumper.findByName("testInsert")
        assert dom2.id == jumper.id
    }

    void testUpdate() {
        when:
        initData()
        def jump = Jumper.findByName("jumper1")

        then:

        assert jump
        def j2 = Jumper.update([id: jump.id, name: "testUpdateXXX"])
        RepoUtil.flushAndClear()
        "testUpdateXXX" == j2.name
        jump.id == j2.id
        def dom2 = Jumper.findByName("testUpdateXXX")
        dom2
    }

    void testRemoveParams() {
        setup:
        initData()
        when:
        def stud = Student.findByName("student1")

        then:
        Student.removeById(stud.id)
        RepoUtil.flushAndClear()
        Student.findByName("student1") == null
    }

    void testRemoveParamsFailed() {
        setup:
        initData()
        when:
        def stud = Student.findByName("student1")

        then:
        try {
            Student.removeById(Student.last().id + 1)
            fail "it was supposed to fail because such id doesn't exist"
        }catch(e){
            assert e != null
            assert e instanceof EntityNotFoundException
            assert e.message.startsWith("testing.Student not found with id ")
        }
    }

    void testGetRepoSetup() {
        assert Jumper.repo.class.name.contains("testing.JumperRepo")
        assert Student.repo.class.name == ("DefaultGormRepo")
    }


}
