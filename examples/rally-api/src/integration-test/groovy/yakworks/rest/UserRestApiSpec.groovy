package yakworks.rest


import org.springframework.http.HttpStatus

import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.api.SpringApplication
import yakworks.rest.client.OkHttpRestTrait

@Integration(applicationClass = SpringApplication)
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
