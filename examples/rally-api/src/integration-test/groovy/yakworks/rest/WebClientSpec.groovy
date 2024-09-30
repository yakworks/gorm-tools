package yakworks.rest


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import yakworks.rest.client.WebClientTrait

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
