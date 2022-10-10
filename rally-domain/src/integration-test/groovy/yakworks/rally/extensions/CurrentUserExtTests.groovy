package yakworks.rally.extensions

import gorm.tools.async.AsyncArgs
import gorm.tools.async.AsyncService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.security.user.CurrentUser
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class CurrentUserExtTests extends Specification implements DomainIntTest {

    CurrentUser currentUser
    AsyncService asyncService

    def "sanity check"() {
        expect:
        currentUser.org
    }

    def "isCustomer"() {
        when:
        assert !currentUser.isCustomer()
        def org = Org.get(1)
        org.type = OrgType.Customer
        org.persist()
        flushAndClear()

        then:
        currentUser.isCustomer()
    }

    def "user in thread"() {
        when:
        def syncOrg = false
        def completableFuture = asyncService.runAsync(AsyncArgs.withSession()) {
            syncOrg = currentUser.org
        }
        completableFuture.join() //wait
        // assert syncOrg

        then:
        currentUser.org
        // syncOrg
    }

    private void doInThread(Closure c) {
        Throwable exception

        Thread.start {
            try {
                c()
            }
            catch (Throwable e) {
                exception = e
            }
        }.join()

        if (exception) {
            throw exception
        }
    }

}
