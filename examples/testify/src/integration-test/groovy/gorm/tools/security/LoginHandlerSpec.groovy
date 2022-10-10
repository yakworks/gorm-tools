package gorm.tools.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.gorm.AppUserService
import yakworks.security.spring.SpringSecService
import yakworks.security.spring.listeners.SecLoginHandler
import yakworks.security.spring.user.SpringUserUtils
import yakworks.testing.gorm.integration.DataIntegrationTest

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
        boolean result = loginHandler.shouldWarnAboutPasswordExpiry(SpringUserUtils.buildSpringUser("admin", "test", [], 1, 1))

        then:
        1 * userService.remainingDaysForPasswordExpiry(_) >> 9
        result == true
    }
}
