package yakworks.security.gorm.store

import java.time.Instant

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.OAuth2AccessToken

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

    def "StoreToken OAuth"() {
        setup:
        AppUser.create(username: 'admin', email: 'foo@foo.com')
        flush()

        when:
        def now = Instant.parse("2022-12-01T23:59:00.00Z");
        def oat = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "yak1234",
            now,
            now.plusSeconds(600)
        )
        tokenStore.storeToken('admin', oat)

        // tokenStore.loadUserByToken("yak1234")

        then:
        tokenStore.loadUserByToken("yak1234").username == "admin"

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
