package gorm.tools.security


import yakworks.gorm.testing.integration.DataIntegrationTest
import gorm.tools.security.services.AppUserService
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
class LoginHandlerSpec extends Specification implements DataIntegrationTest {
    void "test shouldWarnAboutPasswordExpiry"() {
        setup:
        SecLoginHandler loginHandler = new SecLoginHandler()
        AppUserService userService = Mock()
        loginHandler.userService = userService

        loginHandler.passwordExpiryEnabled = true
        loginHandler.passwordWarnDays = 10

        when:
        boolean result = loginHandler.shouldWarnAboutPasswordExpiry(new GrailsUser("admin", "test", true, true, true, true, [], 1))

        then:
        1 * userService.remainingDaysForPasswordExpiry(_) >> 9
        result == true
    }
}
