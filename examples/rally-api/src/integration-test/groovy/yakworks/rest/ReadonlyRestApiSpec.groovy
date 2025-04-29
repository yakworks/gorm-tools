package yakworks.rest

import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

@Integration
class ReadonlyRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/contact"

    void setupSpec() {
        OkAuth.TOKEN = null
    }

    void cleanupSpec() {
        OkAuth.TOKEN = null
    }

    def setup(){
        login("readonly", "123")
    }

    void "create"() {
        when:
        def resp = post(path,  data)

        then:
        assertAccessDenied(resp)
    }

    void "update"() {
        when:
        def resp = put(path+"/1",  data)

        then:
        assertAccessDenied(resp)
    }

    void "upsert"() {
        when:
        def resp = post(path+"/upsert",  data)

        then:
        assertAccessDenied(resp)
    }

    void "remove"() {
        when:
        def resp = delete(path+"/1")

        then:
        assertAccessDenied(resp)
    }

    void "bulk"() {
        when:
        def resp = post(path+"/bulk?jobSource=Oracle&savePayload=false",  [data])

        then:
        assertAccessDenied(resp)
    }

    void assertAccessDenied(Response resp) {
        assert resp
        assert resp.code() == HttpStatus.UNAUTHORIZED.value()

        Map body = bodyToMap(resp)

        assert body
        assert !body.ok
        assert body.code == "error.unauthorized"
        assert body.title == 'Unauthorized'
        assert body.detail == 'Access Denied'
    }

    Map getData() {
        return [name: "C1", firstName: "C1", orgId: 2,]
        //return [num:"T1", name:"T1", type: OrgType.Customer.name()]
    }
}
