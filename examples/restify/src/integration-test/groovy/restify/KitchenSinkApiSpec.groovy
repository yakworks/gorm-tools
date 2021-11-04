package restify

import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import yakworks.commons.json.JsonEngine

@Integration
class KitchenSinkApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/kitchen"

    void "test get"() {
        when:
        def resp = get("$path/1")
        String bodyText = resp.body().string()
        Map body = JsonEngine.parseJson(bodyText, Map)

        then:
        resp.code() == HttpStatus.OK.value()
        // bodyText == "123"
        body.id
        body.name == 'Sink1'

    }

    void "testing post"() {
        when:
        Response resp = post(path, [num: "foobie123", name: "foobie"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'foobie'
        delete(path, body.id)
    }

}
