package yakworks.rest

import org.springframework.http.HttpStatus
import yakworks.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.json.groovy.JsonEngine

@Integration
class ExKitchenSinkApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/kitchen"

    void setup(){
        login()
    }

    @Ignore
    void "test get"() {
        when:
        Response resp = get(getPath('rally/org/1'))
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

    void "post with bindId"() {
        when:
        Response resp = post(path + "?bindId=true", [num: "foobie123", name: "foobie", id:9999])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id == 9999
        body.name == 'foobie'

        cleanup:
        if(body.id) delete(path, body.id)
    }

}
