package yakworks.security

import java.time.Instant
import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.OAuth2AccessToken

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait
import yakworks.security.spring.token.store.TokenStore

import java.time.ZoneId

@Integration
class OpaqueRestApiSpec extends Specification implements OkHttpRestTrait {

    @Autowired TokenStore tokenStore

    String endpoint = "/api/rally/user"

    def setup(){
        OkAuth.TOKEN = "opq_123"
    }

    void cleanupSpec() {
        OkAuth.TOKEN = null
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
        //add token to the store.
        LocalDateTime now = LocalDateTime.now()
        Instant nowInstant = now.atZone(ZoneId.of("UTC")).toInstant()
        def oat = createOAuthToken("opq_123", nowInstant, nowInstant.plusSeconds(20))
        tokenStore.storeToken('admin', oat)

        when:
        def resp = get("$endpoint/1")
        assert resp.code() == 200
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        //shoudl not have the display:false fields
        !body.containsKey('passwordHash')
        !body.containsKey('resetPasswordToken')

        cleanup:
        tokenStore.removeToken('opq_123')
    }

}
