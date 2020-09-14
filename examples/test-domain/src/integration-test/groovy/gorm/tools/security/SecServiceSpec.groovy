package gorm.tools.security

import gorm.tools.security.domain.SecUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.testing.SecuritySpecHelper
import gorm.tools.testing.integration.DataIntegrationTest
import gorm.tools.security.services.SecService
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
public class SecServiceSpec extends Specification implements SecuritySpecHelper, DataIntegrationTest  {

    SecService secService

    def setup() {
        authenticate(SecUser.get(1), SecRole.ADMINISTRATOR)
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
        SecUser.get(1) == secService.user
    }

    def testIsLoggedIn() {
        expect:
        secService.isLoggedIn()
    }

    def testIfAllGranted() {
        expect: "All roles of current user"
        secService.ifAllGranted(SecRole.ADMINISTRATOR)
    }

    def testIfAnyGranted(){
        expect:
        secService.ifAnyGranted(SecRole.ADMINISTRATOR, "FakeRole")
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
