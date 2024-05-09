package gorm.tools.transaction

import gorm.tools.hibernate.GormToolsPreQueryEventListener
import gorm.tools.hibernate.QueryTimeoutConfig
import gorm.tools.hibernate.UserQueryTimeoutConfig
import grails.testing.mixin.integration.Integration
import org.springframework.transaction.TransactionTimedOutException
import spock.lang.Ignore
import spock.lang.Specification

import javax.inject.Inject

@Integration
class GormToolsTrxTimeoutSpec extends Specification {

    @Inject GormToolsPreQueryEventListener gormToolsPreQueryEventListener
    @Inject QueryTimeoutConfig queryTimeoutConfig
    @Inject UserQueryTimeoutConfig userQueryTimeoutConfig


    void "sanity check prequery listener"() {
        expect:
        gormToolsPreQueryEventListener
        queryTimeoutConfig.query == 60
        userQueryTimeoutConfig.users.size() == 1
        userQueryTimeoutConfig.users.containsKey 'admin'
        userQueryTimeoutConfig.users.admin.queryTimeout == 120
    }


    @Ignore //cant be tested in gorm-tools - h2 db doesnt provide a way to delay a query similar to pg_sleep
    void "test query timeout"() {
        when:
        timeout(60)

        then: "trx should timeout with 120 sec delay"
        TransactionTimedOutException ex = thrown()
    }
}
