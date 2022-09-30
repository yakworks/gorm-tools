package yakworks.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRolePermission
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

    Authentication getAuth(){
        SecurityContextHolder.context?.authentication
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


        new SecRolePermission(roleAdmin, 'printer:admin').persist()

        new SecRolePermission(roleUser, 'printer:use').persist()


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
