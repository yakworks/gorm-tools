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
        body.title == "Not Found"
        body.detail == 'Org not found for 10001'
    }

    void "test org errors, no type"() {

        when:
        Map invalidData2 = [num:'foo1', name: "foo"]
        Response resp = post('/api/rally/org', invalidData2)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == "Validation Error"
        body.detail == 'OrgSource validation errors'
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
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == "Validation Error"
        body.detail == 'Project validation errors'
        body.errors[0].message == "Property [name] of class [class yakworks.testify.model.Project] cannot be null"
        body.errors[0].field == "name"
        body.errors[1].message == "Property [num] of class [class yakworks.testify.model.Project] cannot be null"
        body.errors[1].field == "num"

    }

    void "test data access exception on db constraint violation"() {
        setup:
        jdbcTemplate.execute("CREATE UNIQUE INDEX project_num_unique ON Project(num)")

        when:
        Response resp = post('/api/project', [ name:"Project-1", num:"P1", inactive: true, billable: true])
        Map body = bodyToMap(resp)
        def orgId = body.id

        then: "1st record inserted successfully"
        resp.code() == HttpStatus.CREATED.value()

        when: "2nd record with duplicate num"
        resp = post('/api/project', [ name:"Project-2", num:"P1", inactive: true, billable: true])
        body = bodyToMap(resp)

        then: "Would cause DataAccessException"
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == "Data Access Exception"
        ((String)body.detail).contains("ConstraintViolationException")
        ((String)body.detail).contains("PROJECT_NUM_UNIQUE")

        delete("/api/project", orgId)
        jdbcTemplate.execute("DROP index project_num_unique")
    }

}
