package gorm.tools.transaction

import gorm.tools.hibernate.GormToolsPreQueryEventListener
import gorm.tools.hibernate.GormToolsTrxManagerBeanPostProcessor
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import org.grails.orm.hibernate.GrailsHibernateTransactionManager
import org.hibernate.QueryTimeoutException
import org.springframework.transaction.TransactionTimedOutException
import org.springframework.transaction.annotation.Propagation
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

import javax.inject.Inject

@Integration
class GormToolsTrxTimeoutSpec extends Specification {

    @Inject GormToolsTrxManagerBeanPostProcessor gormToolsTrxManagerBeanPostProcessor
    @Inject GrailsHibernateTransactionManager grailsHibernateTransactionManager
    @Inject GormToolsPreQueryEventListener gormToolsPreQueryEventListener

    void "sanity check trx timeout"() {
        expect: "should be picked up from config"
        gormToolsTrxManagerBeanPostProcessor.transactionTimeout == 60
        grailsHibernateTransactionManager.defaultTimeout == 60
    }

    void "sanity check prequery listener"() {
        expect:
        gormToolsPreQueryEventListener
        gormToolsPreQueryEventListener.extendedQueryTimeout == 120
        gormToolsPreQueryEventListener.users.size() == 1
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void timeout() {
        Thread.sleep(60 * 1000)
        Org.get(1)
    }

    void "test timeout"() {
        when:
        timeout()

        then:
        TransactionTimedOutException ex = thrown()
    }
}
