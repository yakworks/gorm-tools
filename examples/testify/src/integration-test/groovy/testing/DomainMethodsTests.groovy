package testing

import org.springframework.dao.OptimisticLockingFailureException

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

//tests the persist and remove methods
@Integration
@Rollback
class DomainMethodsTests extends Specification {

    void testPersistFailDataAccess() {
        when:
        def org = Org.create(num:'123', name: 'jumper1', type: "Customer").persist(flush: true)

        Org.executeUpdate("update Org j set j.version=20 where j.name='jumper1'")
        org.name = 'fukt'
        org.persist(flush: true)
        fail "it was supposed to fail the save because of OptimisticLockingFailureException"

        then:
        thrown(OptimisticLockingFailureException)
    }

}
