package gorm.tools.security


import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.testing.gorm.SecuritySpecHelper
import yakworks.testing.gorm.integration.DataIntegrationTest
import yakworks.security.SecService
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
public class SecServiceSpec extends Specification implements SecuritySpecHelper, DataIntegrationTest  {

    SecService secService

    def setup() {
        authenticate(AppUser.get(1), SecRole.ADMIN)
    }

    def testGetPrincipal() {
        expect:
        secService.getPrincipal() != null
    }

    def testGetAuthentication() {
        expect:
        secService.getAuthentication() != null
    }

    def testGetUserId() {
        expect:
        1 == secService.userId
    }

    def testGetUser() {
        expect:
        AppUser.get(1) == secService.user
    }

    def testIsLoggedIn() {
        expect:
        secService.isLoggedIn()
    }

    def testIfAllGranted() {
        expect: "All roles of current user"
        secService.ifAllGranted(SecRole.ADMIN)
    }

    def testIfAnyGranted(){
        expect:
        secService.ifAnyGranted(SecRole.ADMIN, "FakeRole")
    }

    def testIfNotGranted() {
        expect:
        secService.ifNotGranted("fakeRole") == true
    }

    def "test principal roles"() {
        when:
        List roles = secService.principalRoles

        then:
        roles.size() == secService.user.roles.size()
        roles.containsAll(secService.user.roles.name)
    }
}
