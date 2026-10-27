package yakworks.rest

import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.rest.client.OkHttpRestTrait

@Integration
class MassUpdateRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/org"

    void setup(){
        login()
    }

    List<Long> createOrgs(String prefix, int count) {
        (1..count).collect { int i ->
            Response resp = post(path, [num: "$prefix$i", name: "$prefix$i", type: "Customer"])
            assert resp.code() == HttpStatus.CREATED.value()
            bodyToMap(resp).id as Long
        }
    }

    void deleteOrgs(List<Long> ids) {
        ids.each { delete(path, it) }
    }

    void "mass update applies data to all ids"() {
        setup:
        List<Long> ids = createOrgs("massupd-all", 3)

        when:
        Response resp = put("$path/massUpdate", [ids: ids, data: [comments: 'mass-updated']])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.MULTI_STATUS.value()
        body.ok
        !body.problems

        and:
        ids.every { Long id ->
            bodyToMap(get(path, id)).comments == 'mass-updated'
        }

        cleanup:
        deleteOrgs(ids)
    }

    void "mass update with a failing id returns problems for it and updates the rest"() {
        setup:
        List<Long> ids = createOrgs("massupd-partial", 2)

        when:
        Response resp = put("$path/massUpdate", [ids: ids + [999999], data: [comments: 'partial']])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.MULTI_STATUS.value()
        !body.ok
        body.problems.size() == 1
        body.problems[0].code == 'error.notFound'
        body.problems[0].detail.contains('999999')

        and:
        ids.every { Long id ->
            bodyToMap(get(path, id)).comments == 'partial'
        }

        cleanup:
        deleteOrgs(ids)
    }

    void "mass update with empty ids"() {
        when:
        Response resp = put("$path/massUpdate", [ids: [], data: [comments: 'foo']])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.BAD_REQUEST.value()
        !body.ok
        body.code == 'error.data.emptyPayload'
    }

    void "mass update with empty data"() {
        when:
        Response resp = put("$path/massUpdate", [ids: [1], data: [:]])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.BAD_REQUEST.value()
        !body.ok
        body.code == 'error.data.emptyPayload'
    }

}
