package yakworks


import org.springframework.http.HttpStatus

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.api.SpringApplication
import yakworks.rest.client.OkHttpRestTrait

@Integration(applicationClass = SpringApplication)
@Rollback
class SmokeRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally"

    // def setup(){
    //     login()
    // }

    // @Value('${local.server.port}')
    // protected Integer serverPort;

    void "get smoke test"() {
        when:
        Response resp = get("$path/smoke?foo=buzz&bar=baz")

        then:
        resp.body().string() == "hello foo:buzz bar:baz"
        resp.code() == HttpStatus.OK.value()

    }

    void "get smoke test optional params"() {
        when:
        Response resp = get("$path/smoke/optionals?foo=")

        then:
        resp.body().string() == "hello foo: bar:null"
        resp.code() == HttpStatus.OK.value()

    }

    void "get smoke test model"() {
        when:
        Response resp = get("$path/smoke/model?foo=buzz&bar=baz")

        then:
        resp.code() == HttpStatus.OK.value()
        resp.body().string() == 'hello SmokeController$ParmsModel(buzz, baz)'
    }

    void "testing post"() {
        when:
        Response resp = post("$path/smoke?foo=buzz", [num: "123", name: "up"])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.num
        body.foo == "buzz"
        body.name == 'up'

    }

}
