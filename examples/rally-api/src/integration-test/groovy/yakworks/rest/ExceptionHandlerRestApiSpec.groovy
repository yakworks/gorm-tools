package yakworks.rest

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus

import gorm.tools.transaction.WithTrx
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.rest.client.OkHttpRestTrait

import static yakworks.etl.excel.ExcelUtils.getHeader

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
        //TODO This is generating an object that is converetd to rest object, where and can we intercept it?
        // [timestamp:1732731709097, status:500, error:Internal Server Error, path:/api/rally/exceptionTest/throwable]
        // what do we need to do to intercept that?
        //shows javax.servlet.ServletException: Could not resolve view with name '/error' in servlet with name 'grailsDispatcherServlet'
        //I checked in a error.hbs, it looks like we can probably set that up, just not sure about the model.

        body.ok == false
        body.status == 500
        body.code == "error.unexpected"
        body.detail.contains('nested exception is Assertion failed')
    }
}
