package gorm.tools.transaction

import gorm.tools.hibernate.QueryConfig
import gorm.tools.mango.api.QueryArgs
import yakworks.security.gorm.UserSecurityConfig
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
class GormToolsQueryTimeoutSpec extends Specification {

    @Inject QueryConfig queryConfig
    @Inject UserSecurityConfig userSecurityConfig
    @Inject SecService secService


    void "test max"() {
        when:
        List results = Org.query(QueryArgs.of(max:5))

        then:
        results.size() == 5
    }

    void "test max with smaller value thn default max configured"() {
        when:
        List results = Org.list(max:10)

        then:
        results.size() == 10
    }

    @Ignore("Max not implemented")
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
