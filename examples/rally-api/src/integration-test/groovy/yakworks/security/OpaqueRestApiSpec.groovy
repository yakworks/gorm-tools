package yakworks.security

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.OAuth2AccessToken

import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait
import yakworks.security.gorm.model.AppUserToken
import yakworks.security.spring.token.store.TokenStore

// @Ignore
@Integration
class OpaqueRestApiSpec extends Specification implements OkHttpRestTrait {

    @Autowired TokenStore tokenStore

    String endpoint = "/api/rally/user"

    def setup(){
        OkAuth.TOKEN = "opq_123"
        //OkAuth.BEARER_TOKEN = "Bearer opq_123"
    }

    OAuth2AccessToken createOAuthToken(String tokenValue, Instant nowTime, Instant expireAt){
        def oat = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            tokenValue,
            nowTime,
            expireAt
        )
        return oat
    }

    void "test get to make sure display false dont get returned"() {
        setup:
        // AppUserToken.create([username: 'admin', tokenValue: 'opq_123', expiresAt: LocalDateTime.now().plusDays(2)], flush: true)
        //add token to the store.
        def oat = createOAuthToken("opq_123", Instant.now(), Instant.now().plusSeconds(30))
        tokenStore.storeToken('admin', oat)

        when:
        def resp = get("$endpoint/1")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        //shoudl not have the display:false fields
        !body.containsKey('passwordHash')
        !body.containsKey('resetPasswordToken')
    }

}
