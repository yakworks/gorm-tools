package yakworks

import org.springframework.http.HttpStatus

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import yakworks.rest.client.OkHttpRestTrait

@Integration //
@Rollback
class SmokeRestApiSpec extends Specification implements OkHttpRestTrait {

    //@Autowired DefaultDataBinderFactory defaultDataBinderFactory

    String path = "/api/rally"

     void setup(){
         login()
     }

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
        //assert defaultDataBinderFactory
        Response resp = get("$path/smoke/model?foo=buzz&bar=baz")

        then:
        resp.body().string() == 'hello SmokeController$ParmsModel(buzz, baz)'
        resp.code() == HttpStatus.OK.value()
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
