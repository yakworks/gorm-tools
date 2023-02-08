package yakworks.rest

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

import yakworks.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class UserRestApiSpec extends Specification implements OkHttpRestTrait {

    String endpoint = "/api/rally/user"

    def setup(){
        login()
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
