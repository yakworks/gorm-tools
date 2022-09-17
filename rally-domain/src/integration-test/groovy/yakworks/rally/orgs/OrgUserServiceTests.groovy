package yakworks.rally.orgs

import gorm.tools.async.AsyncConfig
import gorm.tools.async.AsyncService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class OrgUserServiceTests extends Specification implements DomainIntTest {

    UserOrgService userOrgService
    AsyncService asyncService

    def "sanity check"() {
        expect:
        userOrgService.userOrg
    }

    def "user in thread"() {
        when:
        def syncOrg = false
        def completableFuture = asyncService.runAsync(AsyncConfig.withSession()) {
            syncOrg = userOrgService.userOrg
        }
        completableFuture.join() //wait
        // assert syncOrg

        then:
        userOrgService.userOrg
        // syncOrg
    }

    // def "user in old thread"() {
    //     when:
    //     assert springSecurityService.loggedIn
    //
    //     doInThread {
    //         Org.withTransaction {
    //             assert springSecurityService.loggedIn
    //             assert userOrgService.userOrg
    //         }
    //     }
    //
    //     then:
    //     userOrgService.userOrg
    //     // syncOrg
    // }

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
