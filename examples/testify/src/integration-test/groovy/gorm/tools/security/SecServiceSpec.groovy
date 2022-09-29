package gorm.tools.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.SecService
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.testing.gorm.integration.DataIntegrationTest
import yakworks.testing.gorm.integration.SecuritySpecHelper

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
        1 == secService.currentUser.userId
    }

    def testIfNotGranted() {
        expect:
        secService.ifNotGranted("fakeRole") == true
    }

}
