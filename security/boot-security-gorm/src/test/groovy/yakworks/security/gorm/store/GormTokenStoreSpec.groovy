package yakworks.security.gorm.store

import yakworks.security.PasswordConfig

import java.time.Instant

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException

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
        passwordConfig(PasswordConfig)
        passwordValidator(PasswordValidator)
    }}

    OAuth2AccessToken createOAuthToken(String tokenValue, Instant issuedAt = Instant.now(), Instant expireAt = Instant.now().plusSeconds(30)){
        def oat = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            tokenValue,
            issuedAt,
            expireAt
        )
        return oat
    }

    def "StoreToken and LoadUserByToken"() {
        setup:
        AppUser.create(username: 'admin', email: 'foo@foo.com')
        flush()

        when:
        def oat = createOAuthToken("yak1234", Instant.now(), Instant.now().plusSeconds(30))
        tokenStore.storeToken("admin", oat)

        then:
        tokenStore.loadUserByToken("yak1234").username == "admin"
    }

    def "StoreToken OAuth"() {
        setup:
        AppUser.create(username: 'admin', email: 'foo@foo.com')
        flush()

        when:
        def oat = createOAuthToken("yak1234")
        tokenStore.storeToken('admin', oat)

        then:
        tokenStore.loadUserByToken("yak1234").username == "admin"
    }

    def "RemoveToken"() {
        when:
        def oat = createOAuthToken("yak1234")
        tokenStore.storeToken("admin", oat)

        then:
        tokenStore.removeToken("yak1234")
    }

    def "FindUsernameForExistingToken"() {
        when:
        def oat = createOAuthToken("yak1234")
        tokenStore.storeToken("admin", oat)

        then: "should find it"
        tokenStore.findUsernameForExistingToken("yak1234") == 'admin'

        and: "should return null when not found"
        !tokenStore.findUsernameForExistingToken("yak123456")
    }

    def "should throw BadOpaqueTokenException when not found"() {
        when:
        tokenStore.loadUserByToken("invalid token")

        then: "should fire ex"
        thrown(BadOpaqueTokenException)

    }

    def "should throw BadOpaqueTokenException when expired"() {
        when:
        def issuedAt = Instant.parse("2022-12-01T23:59:00.00Z");
        def oat = createOAuthToken("opq_1234_expired", issuedAt, issuedAt.plusSeconds(30))
        tokenStore.loadUserByToken("opq_1234_expired")

        then: "should fire ex"
        thrown(BadOpaqueTokenException)

    }
}
