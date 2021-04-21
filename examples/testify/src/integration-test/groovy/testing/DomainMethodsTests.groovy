package testing

import org.springframework.dao.OptimisticLockingFailureException

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testify.model.Project

//tests the persist and remove methods
@Integration
@Rollback
class DomainMethodsTests extends Specification {

    void testPersistFailDataAccess() {
        when:
        def jump = new Project(num:'123', name: 'jumper1').persist(flush: true)
        then:
        try {
            Project.executeUpdate("update Project j set j.version=20 where j.name='jumper1'")
            jump.name = 'fukt'
            jump.persist(flush: true)
            fail "it was supposed to fail the save because of OptimisticLockingFailureException"
        } catch (OptimisticLockingFailureException e) {
            //assert e.message == "Another user has updated the skydive.Jumper while you were editing"
            assert e.message.contains('optimistic locking failed')
        }
    }

}
