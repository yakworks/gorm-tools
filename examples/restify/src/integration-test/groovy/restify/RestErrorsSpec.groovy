package restify

import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@Integration
class RestErrorsSpec extends Specification implements OkHttpRestTrait {
    JdbcTemplate jdbcTemplate

    void "entity not found"() {

        when:
        Map invalidData2 = [num:'foo1', name: "foo"]
        Response resp = get('/api/rally/org/10001')
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.NOT_FOUND.value()
        body.status == HttpStatus.NOT_FOUND.value()
        body.title == "Org not found with id 10001"
        // body.detail == 'Org not found for 10001'
    }

    void "test org errors, no type"() {

        when:
        Map invalidData2 = [num:'foo1', name: "foo"]
        Response resp = post('/api/rally/org', invalidData2)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == "Org Validation Error(s)"
        body.errors.size() == 1
        body.errors.find{ it.field == 'type' }
    }

    void 'test post errors on org'() {

        when:
        Map invalidData2 = [
            type: "Customer"
        ]
        Response resp = post('/api/rally/org', invalidData2)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == "Org Validation Error(s)"
        body.errors[0].code == 'nullable'
        body.errors[0].message == "Property [name] of class [class yakworks.rally.orgs.model.Org] cannot be null"
        body.errors[0].field == "name"
        body.errors[1].code == 'nullable'
        body.errors[1].message == "Property [num] of class [class yakworks.rally.orgs.model.Org] cannot be null"
        body.errors[1].field == "num"

    }

    void "test data access exception on db constraint violation"() {
        when:
        Response resp = post('/api/rally/org', [ name:"Project-1", num:"P1", type: "Customer"])
        Map body = bodyToMap(resp)
        def orgId = body.id

        then: "1st record inserted successfully"
        resp.code() == HttpStatus.CREATED.value()

        when: "2nd record with duplicate num"
        resp = post('/api/rally/org', [ name:"Project-2", num:"P1",type: "Customer"])
        body = bodyToMap(resp)

        then: "Would cause DataAccessException"
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == "Data Access Exception"
        ((String)body.detail).contains("ConstraintViolationException")
        ((String)body.detail).contains("IX_ORGSOURCE_UNIQUE")

        delete("/api/rally/org", orgId)
    }

}
