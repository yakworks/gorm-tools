package gorm.tools.transaction

import gorm.tools.hibernate.GormToolsPreQueryEventListener
import gorm.tools.hibernate.GormToolsTrxManagerBeanPostProcessor
import gorm.tools.hibernate.QueryTimeoutConfig
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import org.grails.orm.hibernate.GrailsHibernateTransactionManager
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
    @Inject QueryTimeoutConfig queryTimeoutConfig

    void "sanity check trx timeout"() {
        expect: "should be picked up from config"
        gormToolsTrxManagerBeanPostProcessor.queryTimeoutConfig.transaction == 120
        grailsHibernateTransactionManager.defaultTimeout == 120
    }

    void "sanity check prequery listener"() {
        expect:
        gormToolsPreQueryEventListener
        queryTimeoutConfig.query == 60
        queryTimeoutConfig.transaction == 120
        queryTimeoutConfig.users.size() == 1
        queryTimeoutConfig.users.containsKey 'admin'
        queryTimeoutConfig.users.admin.queryTimeout == 120
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void timeout(int seconds) {
        Thread.sleep(seconds * 1000)
        Org.get(1)
    }

    void "test trx timeout"() {
        when:
        timeout(121)

        then: "trx should timeout with 120 sec delay"
        TransactionTimedOutException ex = thrown()
    }

    //XXX @Josh here's the problem
    //Every time before firing a query, hibernate calculates remaning time for the query based on the trx timeout
    //Hence, whatever value is set as query timeout will always get overriden when we have trx timeout
    //See StatementPreparerImpl.java line 200
    void "test query timeout"() {
        when:
        timeout(60)

        then: "trx should timeout with 120 sec delay"
        TransactionTimedOutException ex = thrown()
    }
}
