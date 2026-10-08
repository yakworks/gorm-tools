package yakworks.rest

import gorm.tools.transaction.WithTrx
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import yakworks.rest.client.OkHttpRestTrait

@Integration
class ExceptionHandlerRestApiSpec extends Specification implements OkHttpRestTrait, WithTrx {

    String path = "/api/rally/exceptionTest"

    def setup(){
        login()
    }

    void "test unexpected exception handler - runtimeException"() {
        when:
        Response resp = post(path+"/runtimeException", [num:"C1", name:"C1", type: 'Customer'])
        Map body = bodyToMap(resp)

        then:
        body
        body.ok == false
        body.status == 500
        body.code == "error.unexpected"
        body.detail == "Test error"
    }

    void "test expected exception handler - dataProblem"() {
        when:
        Response resp = post(path+"/dataProblem", [num:"C1", name:"C1", type: 'Customer'])
        Map body = bodyToMap(resp)

        then:
        body
        body.ok == false
        body.status == 400
        body.code == "error.data.problem"
        body.detail == "test"
    }

    void "test exception handler - throwable"() {
        when:
        Response resp = post(path+"/throwable", [num:"C1", name:"C1", type: 'Customer'])
        Map body = bodyToMap(resp)

        then:
        body.ok == false
        body.status == 500
        body.code == "error.unexpected"
        body.detail.contains('nested exception is Assertion failed')
    }

    void "request method not supported - login"() {
        when:
        Response resp = get("/api/oauth/token")
        Map body = bodyToMap(resp)

        then:
        body
        body.containsKey('ok')
        !body.ok
        body.code
    }
}
