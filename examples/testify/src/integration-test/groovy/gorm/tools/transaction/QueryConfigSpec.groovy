package gorm.tools.transaction

import gorm.tools.hibernate.QueryConfig
import yakworks.security.gorm.UserSecurityConfig
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.SecService

import javax.inject.Inject

@Integration
@Rollback
class QueryConfigSpec extends Specification {

    @Inject QueryConfig queryConfig
    @Inject UserSecurityConfig userSecurityConfig
    @Inject SecService secService
    @Inject SecurityVa

    void "sanity check"() {
        expect:
        queryConfig.timeout == 60
        queryConfig.max == 100
        userSecurityConfig.users.size() == 1
        userSecurityConfig.users.containsKey 'system'
        userSecurityConfig.users.system.query.timeout == 120
        userSecurityConfig.users.system.query.max == 500
    }

}
