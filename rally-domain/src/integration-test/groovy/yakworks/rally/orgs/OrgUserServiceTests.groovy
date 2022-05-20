package yakworks.rally.orgs

import gorm.tools.async.AsyncConfig
import gorm.tools.async.AsyncService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.commons.lang.EnumUtils
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.OrgType

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
        assert syncOrg

        then:
        syncOrg
    }

}
