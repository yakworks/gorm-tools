package gorm.tools.security

import yakworks.security.spring.SpringSecUser
import yakworks.security.spring.listeners.SecLoginHandler
import yakworks.testing.gorm.integration.DataIntegrationTest
import yakworks.security.gorm.AppUserService
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
        boolean result = loginHandler.shouldWarnAboutPasswordExpiry(new SpringSecUser("admin", "test", [], 1))

        then:
        1 * userService.remainingDaysForPasswordExpiry(_) >> 9
        result == true
    }
}
