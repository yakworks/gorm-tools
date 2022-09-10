package yakworks.security


import gorm.tools.security.domain.AppUser
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification
import yakworks.testing.gorm.DomainIntTest
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

/**
 * Proof of concept.
 */
// @Rollback
// @WebAppConfiguration
// @SpringBootTest
// @ContextConfiguration(
//     loader = GrailsApplicationContextLoader,
//     classes = [Application]
// )
@Integration
@Rollback
class CurrentUserSpec extends Specification implements DomainIntTest {//, ApplicationContextAware {

    // ApplicationContext ctx

    @Autowired
    WebApplicationContext ctx

    @Autowired
    CurrentUser currentUser

    // @Before
    // void controllerIntegrationSetup() {
    //     MockRestRequest request = new MockRestRequest(ctx.servletContext)
    //     MockRestResponse response = new MockRestResponse()
    //     GrailsWebMockUtil.bindMockWebRequest(ctx, request, response)
    //     // currentRequestAttributes.setControllerName(controllerName)
    // }

    // void setApplicationContext(ApplicationContext ctx) {
    //     this.ctx = ctx
    // }

    void "getUserMap admin"() {

        when:
        def userMap = currentUser.getUserMap()

        then:
        currentUser.org.id == 2
        userMap.username == 'admin'
    }

    void "getUserMap for customer user"() {
        when:
        def org = Org.get(2)
        org.type = OrgType.Customer
        org.persist(flush: true)
        authenticate(AppUser.get(1), Roles.CUSTOMER)
        def userMap = currentUser.getUserMap()

        then:
        currentUser.org.id == 2
        userMap.isCustomer
        currentUser.isCustomer()
    }

}
