package gorm.tools.transaction

import gorm.tools.hibernate.GormToolsPreQueryEventListener
import gorm.tools.hibernate.QueryConfig
import yakworks.rally.security.UserSecurityConfig
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.transaction.TransactionTimedOutException
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.security.SecService

import javax.inject.Inject

@Integration
@Rollback
class GormToolsQueryListenerSpec extends Specification {

    @Inject GormToolsPreQueryEventListener gormToolsPreQueryEventListener
    @Inject QueryConfig queryConfig
    @Inject UserSecurityConfig userSecurityConfig
    @Inject SecService secService


    void "sanity check"() {
        expect:
        gormToolsPreQueryEventListener
        queryConfig.timeout == 60
        queryConfig.max == 100
        userSecurityConfig.users.size() == 1
        userSecurityConfig.users.containsKey 'system'
        userSecurityConfig.users.system.queryTimeout == 120
        userSecurityConfig.users.system.queryMax == 500
    }

    @Ignore("cant be tested in gorm-tools - h2 db doesnt provide any way to delay a query similar to pg_sleep")
    void "test query timeout"() {
        when:
        timeout(60)

        then: "trx should timeout with 120 sec delay"
        TransactionTimedOutException ex = thrown()
    }

    @Ignore("Max not implemented")
    void "test max"() {
        setup:
        queryConfig.max = 5

        when:
        List results = Org.list()

        then:
        results.size() == 5

        cleanup:
        queryConfig.max = 100
    }

    void "test max with smaller value thn default max configured"() {
        when:
        List results = Org.list(max:10)

        then:
        results.size() == 10
    }

    void "user specific max"() {
        setup: "login as admin"
        userSecurityConfig.users['system'].queryMax = 20
        secService.loginAsSystemUser()

        when: "60 secs"
        List results = Org.list()

        then:
        noExceptionThrown()
        results.size() == 20

        cleanup:
        userSecurityConfig.users['system'].queryMax = 1000
    }
}
