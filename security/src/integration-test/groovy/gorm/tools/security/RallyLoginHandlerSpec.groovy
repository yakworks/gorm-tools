package gorm.tools.security

import gorm.tools.security.RallyLoginHandler
import gorm.tools.testing.integration.DataIntegrationTest
import grails.plugin.rally.security.UserService
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class RallyLoginHandlerSpec extends Specification implements DataIntegrationTest {
    void "test shouldWarnAboutPasswordExpiry"() {
        setup:
        RallyLoginHandler loginHandler = new RallyLoginHandler()
        UserService userService = Mock()
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
