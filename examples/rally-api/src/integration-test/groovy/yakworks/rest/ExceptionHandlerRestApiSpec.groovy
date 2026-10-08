package yakworks.rest

import gorm.tools.transaction.WithTrx
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Ignore
import spock.lang.Specification

import yakworks.rest.client.OkHttpRestTrait

@Integration
class ExceptionHandlerRestApiSpec extends Specification implements OkHttpRestTrait, WithTrx {

    String path = "/api/rally/exceptionTest"

    def setup(){
        login()
    }

    //FIXME @SUD - 404 doesnot return our standard json response
    //currently its being handled by spring's "BasicErrorController"
    @Ignore
    void "error 404"() {
        when:
        Response resp = get("/api/security-tests/unknown")
        Map body = bodyToMap(resp)

        then:
        body
        body.stats == 404
        body.code == 'error.notFound'
        body.title
        body.detail
    }

    //This would be handled by error.hbs,
    //as SecurityTestsController doesnt extend base controllers, and there's no handleException so it wont go through problemHandler
    void "error hbs 500"() {
        when:
        Response resp = get("/api/security-tests/error500")
        Map body = bodyToMap(resp)

        then:
        body
        body.code == 'error.unexpected'
        body.status == 500
        body.title == 'Unexpected Exception'
        body.detail == 'simulate unknown error'
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
}
