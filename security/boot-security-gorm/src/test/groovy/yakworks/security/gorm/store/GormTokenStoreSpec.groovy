package yakworks.security.gorm.store

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.security.gorm.AppUserDetailsService
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.AppUserToken
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.services.PasswordValidator
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class GormTokenStoreSpec extends Specification implements GormHibernateTest, SecurityTest  {
    static List entityClasses = [AppUserToken, AppUser, SecRole, SecRoleUser]

    @Autowired GormTokenStore tokenStore

    Closure doWithGormBeans() { { ->
        tokenStore(GormTokenStore)
        userDetailsService(AppUserDetailsService)
        passwordValidator(PasswordValidator)
    }}

    def "StoreToken and LoadUserByToken"() {
        setup:
        AppUser.create(username: 'admin', email: 'foo@foo.com')
        flush()

        when:
        tokenStore.storeToken("admin", "yak123")

        then:
        tokenStore.loadUserByToken("yak123").username == "admin"
    }

    def "RemoveToken"() {
        when:
        tokenStore.storeToken("admin", "yak123")

        then:
        tokenStore.removeToken("yak123")
    }

    def "FindUsernameForExistingToken"() {
        when:
        tokenStore.storeToken("admin", "yak123")

        then:
        tokenStore.findUsernameForExistingToken("yak123") == 'admin'
    }
}
