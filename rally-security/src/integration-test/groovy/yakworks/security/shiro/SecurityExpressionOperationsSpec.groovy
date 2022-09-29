package yakworks.security.shiro


import org.apache.shiro.realm.Realm
import org.apache.shiro.util.ThreadContext
import org.apache.shiro.web.mgt.WebSecurityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler
import org.springframework.security.access.expression.SecurityExpressionOperations
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.FilterInvocation

import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRolePermission
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.gorm.model.SecUserPermission
import yakworks.security.spring.SpringSecService
import yakworks.security.user.CurrentUser

@Integration
@Rollback
class SecurityExpressionOperationsSpec extends Specification {

    WebSecurityManager shiroSecurityManager
    Realm springSecurityRealm
    SpringSecService secService
    @Autowired AbstractSecurityExpressionHandler securityExpressionHandler
    @Autowired CurrentUser currentUser

    // private request = new MockHttpServletRequest()
    // private response = new MockHttpServletResponse()

    void setup() {
        assert securityExpressionHandler
        ThreadContext.bind shiroSecurityManager
        logout()
    }

    private void login(String username) {
        secService.reauthenticate(username, 'password')
    }

    private void logout() {
        currentUser.logout()
    }

    Authentication getAuth(){
        SecurityContextHolder.context?.authentication
    }

    void "sanity check"() {
        setup:
        setupPerms()
        expect:
        securityExpressionHandler
    }

    void "test hasRole"() {
        setup:
        setupPerms()
        login "userAdmin"

        when:
        def fi = new FilterInvocation('currentUser', 'hasRole')
        def ctx = securityExpressionHandler.createEvaluationContext(getAuth(), fi)
        SecurityExpressionOperations secExp = (SecurityExpressionOperations)ctx.getRootObject().getValue()

        then:
        secExp
        secExp.hasRole('ADMIN')
        secExp.hasRole('MGR')
        !secExp.hasRole('CUST')
        secExp.hasAnyRole('ADMIN', 'CUST')

    }

    @Transactional
    void setupPerms(){
        def save = { it.save(failOnError: true) }
        //these are on top of whats done in BootStrap, so keep that in mind, an admin user already exists for example
        AppUser admin = AppUser.create(getUserParams('userAdmin'))
        AppUser user2 = AppUser.create(getUserParams('user2'))
        AppUser user3 = AppUser.create(getUserParams('user3'))
        SecRole roleAdmin = SecRole.create(code: "ADMIN")
        SecRole roleMgr = SecRole.create(code: "MGR")
        SecRole roleUser = SecRole.create(code: 'CUST')

        save new SecUserPermission(admin, 'printer:print:*')

        save new SecUserPermission(user2, 'printer:print:*')
        save new SecUserPermission(user2, 'printer:maintain:epsoncolor')
        save new SecUserPermission(user2, 'action:kick')

        save new SecUserPermission(user3, 'action:jump')
        save new SecUserPermission(user3, 'action:kick')

        save new SecRolePermission(roleAdmin, 'printer:admin')
        save new SecRolePermission(roleUser, 'printer:use')

        SecRoleUser.create admin, roleAdmin, true
        SecRoleUser.create admin, roleMgr, true
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
