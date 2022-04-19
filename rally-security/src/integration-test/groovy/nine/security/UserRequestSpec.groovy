package nine.security

import javax.inject.Provider

import gorm.tools.security.domain.AppUser
import grails.boot.test.GrailsApplicationContextLoader
import grails.gorm.transactions.Rollback
import nine.rally.testing.DomainIntTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification
import yakworks.rally.tenant.UserRequest
import yakworks.security.Roles

// @Rollback
@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(
    loader = GrailsApplicationContextLoader,
    classes = [Application]
)
@Rollback
class UserRequestSpec extends Specification implements DomainIntTest {//, ApplicationContextAware {

    // ApplicationContext ctx

    @Autowired
    WebApplicationContext ctx

    @Autowired(required = false)
    Provider<UserRequest> userRequest

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
        UserRequest ureq = userRequest.get()
        def userMap = ureq.getUserMap()

        then:
        ureq.org.id == 2
        RequestContextHolder.currentRequestAttributes()
        userMap.username == 'admin'
    }

    void "getUserMap for customer user"() {
        when:
        authenticate(AppUser.get(51), Roles.CUSTOMER)
        UserRequest ureq = userRequest.get()
        def userMap = ureq.getUserMap()

        then:
        ureq.org.id == 205
        ureq.isCustomer()
        userMap.username == 'gbcust'
    }

}
