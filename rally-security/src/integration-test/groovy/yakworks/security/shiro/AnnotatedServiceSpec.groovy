package yakworks.security.shiro

import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.SecService
import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import org.apache.shiro.authz.UnauthenticatedException
import org.apache.shiro.authz.UnauthorizedException
import org.apache.shiro.realm.Realm
import org.apache.shiro.util.ThreadContext
import org.apache.shiro.web.mgt.WebSecurityManager
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.security.gorm.model.SecRolePermission
import yakworks.security.gorm.model.SecUserPermission

@Integration
@Rollback
class AnnotatedServiceSpec extends Specification {

    WebSecurityManager shiroSecurityManager
    Realm springSecurityRealm
    SecService secService
    @Autowired TestService testService


    // private request = new MockHttpServletRequest()
    // private response = new MockHttpServletResponse()

    void setup() {
        assert shiroSecurityManager
        ThreadContext.bind shiroSecurityManager
        logout()
    }

    private void login(String username) {
        secService.reauthenticate(username, 'password')
    }

    private void logout() {
        secService.logout()
    }

    void "sanity check"() {
        setup:
        setupPerms()
        expect:
        testService
    }

    void testOnePermission() {
        setup:
        setupPerms()
        logout()

        when:
        testService.adminPrinter()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'
        testService.adminPrinter()

        then:
        thrown UnauthorizedException

        when:
        logout()
        login 'user2'

        then:
        testService.adminPrinter()

        when:
        logout()
        testService.adminPrinter()

        then:
        thrown UnauthenticatedException
    }

    void testRequireTwoPermissions() {
        setup:
        setupPerms()

        when:
        testService.requireJumpAndKick()

        then:
        thrown UnauthenticatedException

        when:
        login 'user2'
        testService.requireJumpAndKick()

        then:
        thrown UnauthorizedException

        when:
        logout()
        login 'user3'

        then:
        testService.requireJumpAndKick()

        when:
        logout()
        testService.requireJumpAndKick()

        then:
        thrown UnauthenticatedException
    }

    void testRequireOneOrTwoPermissions() {
        setup:
        setupPerms()

        when:
        testService.requireJumpOrKick()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'
        testService.requireJumpOrKick()

        then:
        thrown UnauthorizedException

        when:
        logout()
        login 'user2'

        then:
        testService.requireJumpOrKick()

        when:
        logout()
        login 'user3'

        then:
        testService.requireJumpOrKick()

        when:
        logout()
        testService.requireJumpOrKick()

        then:
        thrown UnauthenticatedException
    }

    void testNonexistentPermissions() {
        setup:
        setupPerms()

        when:
        testService.impossiblePermissions()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'
        testService.impossiblePermissions()

        then:
        thrown UnauthorizedException
    }

    void testRolePermissions() {
        setup:
        setupPerms()

        when:
        testService.requirePrinterAdminPermissions()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'

        then:
        testService.requirePrinterAdminPermissions()

        when:
        testService.requireUsePrinterPermissions()

        then:
        thrown UnauthorizedException

        when:
        logout()
        login 'user2'

        then:
        testService.requirePrinterAdminPermissions()
        testService.requireUsePrinterPermissions()

        when:
        logout()
        login 'user3'
        testService.requirePrinterAdminPermissions()

        then:
        thrown UnauthorizedException

        when:
        testService.requireUsePrinterPermissions()

        then:
        thrown UnauthorizedException
    }

    void testRequiresUser() {
        setup:
        setupPerms()

        when:
        testService.requireUser()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'

        then:
        testService.requireUser()
    }

    void testRequireOneOrTwoRoles() {
        setup:
        setupPerms()

        when:
        testService.requireUserOrAdmin()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'

        then:
        testService.requireUserOrAdmin()

        when:
        logout()
        login 'user2'

        then:
        testService.requireUserOrAdmin()

        when:
        logout()
        login 'user3'
        testService.requireUserOrAdmin()

        then:
        thrown UnauthorizedException
        logout()

        when:
        testService.requireUserOrAdmin()

        then:
        thrown UnauthenticatedException
    }

    void testRequireTwoRoles() {
        setup:
        setupPerms()

        when:
        testService.requireUserAndAdmin()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'
        testService.requireUserAndAdmin()

        then:
        thrown UnauthorizedException

        when:
        logout()
        login 'user2'

        then:
        testService.requireUserAndAdmin()

        when:
        logout()
        login 'user3'
        testService.requireUserAndAdmin()

        then:
        thrown UnauthorizedException

        when:
        logout()
        testService.requireUserAndAdmin()

        then:
        thrown UnauthenticatedException
    }

    void testRequiresGuest() {
        setup:
        setupPerms()

        expect:
        testService.requireGuest()

        when:
        login 'user1'
        testService.requireGuest()

        then:
        UnauthenticatedException e = thrown()
        e.message.startsWith 'Attempting to perform a guest-only operation'
    }

    void testRequiresAuthentication() {
        setup:
        setupPerms()

        when:
        testService.requireAuthentication()

        then:
        thrown UnauthenticatedException

        when:
        login 'user1'

        then:
        testService.requireAuthentication()
    }

    @Transactional
    void setupPerms(){
        def save = { it.save(failOnError: true) }

        AppUser user1 = AppUser.create(getUserParams('user1'))
        AppUser user2 = AppUser.create(getUserParams('user2'))
        AppUser user3 = AppUser.create(getUserParams('user3'))
        SecRole roleAdmin = SecRole.create(code: "ADMIN")
        SecRole roleUser = SecRole.create(code: 'CUST')

        save new SecUserPermission(user1, 'printer:print:*')

        save new SecUserPermission(user2, 'printer:print:*')
        save new SecUserPermission(user2, 'printer:maintain:epsoncolor')
        save new SecUserPermission(user2, 'action:kick')

        save new SecUserPermission(user3, 'action:jump')
        save new SecUserPermission(user3, 'action:kick')

        save new SecRolePermission(roleAdmin, 'printer:admin')
        save new SecRolePermission(roleUser, 'printer:use')

        SecRoleUser.create user1, roleAdmin, true
        SecRoleUser.create user2, roleAdmin, true
        SecRoleUser.create user2, roleUser, true

        // assert 2 == SecRole.count()
        // assert 3 == AppUser.count()
        // assert 3 == SecRoleUser.count()
        // assert 6 == SecUserPermission.count()
    }

    Map getUserParams(String uname){
        Map baseParams = [
            name: uname,
            email:"${uname}@9ci.com",
            username: uname,
            password:'secretStuff',
            repassword:'secretStuff',
        ]
        return baseParams
    }
}
