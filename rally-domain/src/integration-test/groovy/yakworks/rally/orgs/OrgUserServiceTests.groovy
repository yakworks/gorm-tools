package yakworks.rally.orgs

import gorm.tools.async.AsyncConfig
import gorm.tools.async.AsyncService
import grails.gorm.transactions.Rollback
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.mixin.integration.Integration
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import yakworks.commons.lang.EnumUtils
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class OrgUserServiceTests extends Specification implements DomainIntTest {

    UserOrgService userOrgService
    AsyncService asyncService
    SpringSecurityService springSecurityService

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