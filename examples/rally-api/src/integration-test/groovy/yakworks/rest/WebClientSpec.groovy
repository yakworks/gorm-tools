package yakworks.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient

import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait
import yakworks.rest.client.WebClientTrait
import yakworks.security.gorm.model.AppUser
import yakworks.security.user.UserInfo

@Integration
class WebClientSpec extends Specification implements WebClientTrait {

    String endpoint = "/api/rally/user"

    def setup(){
        login()
    }

    void "test webclient"() {
        when:
        ResponseEntity resp = get("$endpoint/1")
        Map user = resp.getBody()

        then:
        resp.statusCode == HttpStatus.OK
        user.id == 1
        user.name == "admin"

    }

}
