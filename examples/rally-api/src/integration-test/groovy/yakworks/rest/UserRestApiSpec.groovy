package yakworks.rest

import org.springframework.http.HttpStatus

import yakworks.gorm.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class UserRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/user"

    void "test get to make sure display false dont get returned"() {
        when:
        def resp = get("$path/1")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        //shoudl not have the display:false fields
        !body.containsKey('passwordHash')
        !body.containsKey('resetPasswordToken')
    }

}
