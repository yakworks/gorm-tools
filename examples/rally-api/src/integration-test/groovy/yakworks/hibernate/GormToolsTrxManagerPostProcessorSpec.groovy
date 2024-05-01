package yakworks.hibernate

import gorm.tools.hibernate.GormToolsTrxManagerBeanPostProcessor
import grails.testing.mixin.integration.Integration
import org.grails.orm.hibernate.GrailsHibernateTransactionManager
import spock.lang.Specification

import javax.inject.Inject

@Integration
class GormToolsTrxManagerPostProcessorSpec extends Specification {

    @Inject GormToolsTrxManagerBeanPostProcessor gormToolsTrxManagerBeanPostProcessor
    @Inject GrailsHibernateTransactionManager grailsHibernateTransactionManager

    void "sanity check timeout"() {
        expect: "should be picked up from config"
        gormToolsTrxManagerBeanPostProcessor.transactionTimeout == 60
        grailsHibernateTransactionManager.defaultTimeout == 60
    }
}
