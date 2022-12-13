package yakworks.rest


import org.springframework.http.HttpStatus

import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

@Ignore
@Integration
class OpaqueRestApiSpec extends Specification implements OkHttpRestTrait {

    String endpoint = "/api/rally/user"

    def setup(){
        OkAuth.TOKEN = "yak_123"
        OkAuth.BEARER_TOKEN = "Bearer yak_123"
    }

    // void "test OkHttpRestTrait login"() {
    //     when:
    //     String token = login('admin', '123')
    //     then:
    //     token
    // }

    void "test get to make sure display false dont get returned"() {
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
