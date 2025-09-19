package yakworks.security

import gorm.tools.transaction.TrxUtils
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRolePermission

@Integration
class ApiPermissionsSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/org"

    void setupSpec() {
        OkAuth.TOKEN = null
    }

    void cleanupSpec() {
        OkAuth.TOKEN = null
    }

    void "unauthorized when user is not logged in"() {
        when: "GET"
        Response resp = get("$path/1")

        then:
        resp.code() == HttpStatus.UNAUTHORIZED.value()
    }

    void "forbidden when user is logged in but does not have permission"() {
        setup:
        login("noroles", "123")
        when: "GET"
        Response resp = get("$path/1")

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "POST"
        resp = post(path, [:])

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "PUT"
        resp = put(path + "/1", [:])

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "DELETE"
        resp = delete(path, 1)

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        cleanup:
        OkAuth.TOKEN = null
    }

    void "logged in and has permission"() {
        setup: "login as admin, it has all the permissions"
        login("admin", "123")

        when: "GET"
        Response resp = get("$path/1")

        then:
        resp.code() == HttpStatus.OK.value()

        when: "POST"
        resp = post(path, [num:'ptest', name:'ptest', type: "Customer"])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id

        when: "PUT"
        resp = put(path + "/${body.id}", [name:"ptest2"])
        body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.name == "ptest2"

        when: "DELETE"
        resp = delete(path, body.id)

        then:
        resp.code() == HttpStatus.NO_CONTENT.value()

        cleanup:
        OkAuth.TOKEN = null
    }


    void "readonly permission"() {
        setup:
        login("readonly", "123")

        when: "READ"
        Response resp = get("$path/1")

        then:
        resp.code() == HttpStatus.OK.value()

        when: "POST"
        resp = post(path, [num:'ptest', name:'ptest', type: "Customer"])

        then: "FORBIDDEN"
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "PUT"
        resp = put(path + "/1", [name:"ptest2"])

        then: "FORBIDDEN"
        resp.code() == HttpStatus.FORBIDDEN.value()

        when: "DELETE"
        resp = delete(path, 1)

        then:
        resp.code() == HttpStatus.FORBIDDEN.value()

        cleanup:
        OkAuth.TOKEN = null
    }

    void "test rpc permission - allowed to admin"() {
        setup: "login as admin, it has all the permissions"
        login("admin", "123")

        when: "GET"
        Response resp = post("$path/rpc?op=rpc1", [:])

        then:
        resp.code() == HttpStatus.OK.value()

        when:
        Map body = bodyToMap(resp)

        then:
        body.ok
        body.rpc == "rpc1"

        cleanup:
        OkAuth.TOKEN = null
    }

    @Rollback
    void "test rpc permission - selected allowed"() {
        setup: "admin has all the permissions"
        SecRole cust = SecRole.query(code:Roles.CUSTOMER).get()

        expect:
        cust

        when: "Add permission to do op=rpc1 only"
        SecRolePermission.withNewTransaction {
            SecRolePermission.create(cust, "rally:org:rpc:rpc1")
        }
        TrxUtils.flush()

        login("cust", "123")

        and: "op=rpc1 allowed"
        Response resp = post("$path/rpc?op=rpc1", [:])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.ok
        body.rpc == "rpc1"

        when: "op=rpc2"
        resp = post("$path/rpc?op=rpc2", [:])

        then: "rpc2 not allowed"
        resp.code() == HttpStatus.FORBIDDEN.value()

        cleanup:
        OkAuth.TOKEN = null

        SecRolePermission.withNewTransaction {
            SecRolePermission.query(role:cust, permission:"rally:org:rpc:rpc1").deleteAll()
        }
    }
}
