package yakworks.security

import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.gorm.model.SecUserPermission
import yakworks.security.user.CurrentUser
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class CurrentUserSpec extends Specification implements DomainIntTest {

    @Autowired CurrentUser currentUser
    // @Autowired SpringSecService springSecService

    // @OnceBefore
    // void doPerms() {
    //     setupPerms()
    // }

    // void setup() {
    //     // ThreadContext.bind shiroSecurityManager
    //     setupPerms()
    //     logout()
    // }

    private void login(String username) {
        secService.login(username)
    }

    private void logout() {
        currentUser.logout()
    }

    void "sanity check"() {
        expect:
        setupPerms()
        login "userAdmin"
        currentUser
    }

    void "test hasRole"() {
        expect:
        setupPerms()
        login "userAdmin"
        currentUser.hasRole('ADMIN')
        currentUser.hasRole('MGR')
        !currentUser.hasRole('CUST')
        currentUser.hasAnyRole(['ADMIN', 'CUST'])
    }

    void "test hasAnyRole string"() {
        expect:
        setupPerms()
        login "userAdmin"
        currentUser.hasAnyRole("ADMIN, CUST")
        currentUser.hasAnyRole("ADMIN")
        !currentUser.hasAnyRole("foo,bar")
    }

    def testIsLoggedIn() {
        expect:
        setupPerms()
        login "userAdmin"
        currentUser.isLoggedIn()
    }

    def testIfAllGranted() {
        expect: "All roles of current user"
        setupPerms()
        login "userAdmin"
        currentUser.hasRole(SecRole.ADMIN)
    }

    def testIfAnyGranted(){
        expect:
        setupPerms()
        login "userAdmin"
        currentUser.hasAnyRole([SecRole.ADMIN, "FakeRole"])
    }

    void "test hasPermission"() {
        setup:
        setupPerms()
        login "userAdmin"

        expect:
        currentUser.hasPermission('printer:print:*')
        currentUser.hasPermission('printer:print:1')

        and:
        !currentUser.hasPermission('printer:poweroff:1')
    }

    void "test hasAnyPermission"() {
        setup:
        setupPerms()
        login "userAdmin"

        expect:
        currentUser.hasAnyPermission(['printer:print:1', 'printer:poweroff:1']) //yes, has 1
        !currentUser.hasAnyPermission(['printer:foo:1', 'printer:poweroff:1']) //nope, none
    }

    // def "test user roles"() {
    //     expect:
    //     roles.size() == currentUser.userInfo.roles.size()
    //     roles.containsAll(currentUser.userInfo.roles)
    // }

    @Transactional
    void setupPerms(){
        //these are on top of whats done in BootStrap, so keep that in mind, an admin user already exists for example
        AppUser admin = AppUser.create(getUserParams('userAdmin'))
        AppUser user2 = AppUser.create(getUserParams('user2'))
        AppUser user3 = AppUser.create(getUserParams('user3'))
        SecRole roleAdmin = SecRole.create(code: "ADMIN")
        SecRole roleMgr = SecRole.create(code: "MGR")
        SecRole roleUser = SecRole.create(code: 'CUST')

        new SecUserPermission(admin, 'printer:print:*').persist()

        new SecUserPermission(user2, 'printer:print:*').persist()

        new SecUserPermission(user2, 'printer:maintain:epsoncolor').persist()

        new SecUserPermission(user2, 'action:kick').persist()

        new SecUserPermission(user3, 'action:jump').persist()

        new SecUserPermission(user3, 'action:kick').persist()

        roleAdmin.addPermission('printer:admin')
        roleAdmin.addPermission('printer:print:*')
        roleAdmin.persist()

        roleUser.addPermission('printer:use')
        roleUser.persist()

        SecRoleUser.create admin, roleAdmin, true
        SecRoleUser.create admin, roleMgr, true
        SecRoleUser.create user2, roleAdmin, true
        SecRoleUser.create user2, roleUser, true

        // assert 2 == SecRole.count()
        // assert 3 == AppUser.count()
        // assert 3 == SecRoleUser.count()
        // assert 6 == SecUserPermission.count()
        flushAndClear()
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
