package yakworks.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait
import yakworks.security.spring.token.store.TokenStore

// @Ignore
@Integration
class OpaqueRestApiSpec extends Specification implements OkHttpRestTrait {

    @Autowired TokenStore tokenStore

    String endpoint = "/api/rally/user"

    def setup(){
        OkAuth.TOKEN = "opq_123"
        OkAuth.BEARER_TOKEN = "Bearer opq_123"
    }

    void "test get to make sure display false dont get returned"() {
        setup:
        //add token to the store.
        tokenStore.storeToken('admin', 'opq_123')
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
