package nine.security


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import nine.rally.testing.DomainIntTest
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import yakworks.rally.tenant.UserTenantResolver

@Integration
@Rollback
class UserTenantResolverSpec extends Specification implements DomainIntTest {//, ApplicationContextAware {

    @Autowired UserTenantResolver userTenantResolver
    // @Autowired SpringSecService secService

    void logout(){
        SecurityContextHolder.context.setAuthentication(null)
        SecurityContextHolder.clearContext()
    }

    void "Test current logged in user is resolved "() {
        when:
        //secService.loginAsSystemUser() //should already be logged in but in case order changes do it
        Long orgId = userTenantResolver.resolveTenantIdentifier()

        then:
        orgId == 2

        // when: "verify AllTenantsResolver::resolveTenantIds"
        // Iterable<Serializable> tenantIds
        // demo.User.withNewSession {
        //     tenantIds = currentUserTenantResolver.resolveTenantIds()
        // }
        //
        // then:
        // tenantIds.toList().size() == 1
        // tenantIds.toList().get(0) == 'admin'

    }

    void "Test Current User throws a TenantNotFoundException if not logged in"() {
        when:
        logout()
        userTenantResolver.resolveTenantIdentifier()

        then:
        def e = thrown(TenantNotFoundException)
        e.message.startsWith("Tenant could not be resolved")
    }

}
