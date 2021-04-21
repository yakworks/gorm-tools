package restify

import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification

@Integration
class RestErrorsSpec extends Specification implements OkHttpRestTrait {

    void "test org errors, no type"() {

        when:
        Map invalidData2 = [num:'foo1', name: "foo"]
        Response resp = post('/api/org', invalidData2)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.total == 1
        body.message == 'OrgSource validation errors'
        // body.errors.find{ it.field == 'link.kind' }.message == 'Property [kind] of class [class yakworks.taskify.domain.Org] cannot be null'
        // body.errors.find{ it.field == 'link.name' }
    }

    void 'test post errors on project'() {

        when:
        Map invalidData2 = [
            inactive: true, billable: true, endDate: '2020-11-11', startDate: '2020-11-11'
        ]
        Response resp = post('/api/project', invalidData2)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.total == 2
        body.message == 'Project validation errors'
        body.errors[0].message == "Property [name] of class [class yakworks.testify.model.Project] cannot be null"
        body.errors[0].field == "name"
        body.errors[1].message == "Property [num] of class [class yakworks.testify.model.Project] cannot be null"
        body.errors[1].field == "num"

    }


}
