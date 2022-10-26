package yakworks.rally.extensions

import gorm.tools.async.AsyncArgs
import gorm.tools.async.AsyncService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.security.spring.user.SpringUserUtils
import yakworks.security.user.CurrentUser
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class UserInfoExtTests extends Specification implements DomainIntTest {

    CurrentUser currentUser
    AsyncService asyncService

    def "sanity check"() {
        expect:
        currentUser.user.org
    }

    def "isCustomer"() {
        when:
        assert !currentUser.user.isCustomer()
        def org = Org.get(2)
        org.type = OrgType.Customer
        org.persist()
        flushAndClear()

        then:
        currentUser.user.isCustomer()
    }

}
