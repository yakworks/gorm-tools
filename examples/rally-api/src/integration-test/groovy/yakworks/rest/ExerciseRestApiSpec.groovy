package yakworks.rest

import org.springframework.http.HttpStatus

import spock.lang.Ignore
import yakworks.commons.map.Maps

import yakworks.rally.orgs.model.Org
import yakworks.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import spock.lang.Unroll
import yakworks.security.gorm.model.AppUser

/**
 * Sanity checks to hit the main endpoints. KISS, keep it simple
 * any special testing logic for an entity should be in its own test
 */
@Integration
class ExerciseRestApiSpec extends Specification implements OkHttpRestTrait {

    String getPath(String entity) { "/api/${entity}" }

    def setup() {
        login()
    }

    void "get index list"() {
        when:
        Response resp = get(getPath('rally/org?q=*'))
        assert resp.code() == HttpStatus.OK.value()
        Map pageMap = bodyToMap(resp)

        then:
        pageMap.data.size() == 20
    }

    void "bad query - json parse failed"() {
        when:
        Response resp = get(getPath('rally/org?q={bad":0}&projections=[{"bad stuff":"sum"}]}'))
        Map pageMap = bodyToMap(resp)

        then:
        pageMap.ok == false
        pageMap.detail.contains "Invalid query expecting current character to be"
    }


    void "bad query - hibernate failed to parse query"() {
        when:
        Response resp = get(getPath('rally/org?q={num:"0"}&projections=[{"num bad":"sum"}]}'))
        Map pageMap = bodyToMap(resp)

        then:
        pageMap.ok == false
        pageMap.detail.contains "Invalid query org.hibernate.hql.internal.ast.QuerySyntaxException"
    }

    @Unroll
    def "LIST get test #entity"(String entity, Class domain) {

        when:
        Response resp = get("${getPath(entity)}")
        Map body = bodyToMap(resp)
        int count
        Org.withNewSession {
            //Need this, coz @Rollback dint work on @Unroll
            count = domain.count()
        }

        then:
        resp.code() == HttpStatus.OK.value()
        body.records == count

        where:
        entity          | domain
        'rally/user'    | AppUser
        'rally/org?q=*' | Org //can have more thn 100 based on order of execution, need to query count
    }

    @Unroll
    def "POST insert: #entity"(String entity, Map data) {

        when:
        Response resp = post("${getPath(entity)}", data)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id
        Maps.mapContains(body, data)
        //clean up
        delete("${getPath(entity)}/${body.id}")

        where:

        entity      | data
        'rally/org' | [num: 'foo1', name: "foo", type: [id: 1]]
        'rally/contact' | [name: "C1", firstName: "C1", orgId: 2,]
        //'user'      | [username:'galt', email: "jim@joe.com", password:'secretStuff', repassword:'secretStuff']
        //'location'  | [city: "Chicago"]
        //'user'      | [name: 'taggy', entityName: 'Customer']
    }

    void "post org with bindId"() {
        when:
        Response resp = post("/api/rally/org?bindId=true", [num: 'org9999', name: "org9999", type: [id: 1], id:9999])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id == 9999

        cleanup:
        if(body.id) delete("${getPath("rally/org")}/${body.id}")
    }

    @Unroll
    def "PUT update: #entity/#id"(String entity, Long id, String prop, String val) {
        setup:
        login()

        when:
        def putData = [(prop): val]
        Response resp = put("${getPath(entity)}/$id", putData)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body[prop] == val

        where:

        entity      | id | prop  | val
        'rally/org' | 1  | 'num' | 'foo123'
        // 'rally/user'     | 1  | 'username' | 'jimmy'
        //        'location' | 1  | 'city' | 'Denver'

    }

    @Ignore //this will no longer work. Requires qSearch to be used now so we can validate q.
    @Unroll
    def "q text search: #entity?q=#qSearch"(String entity, Integer qCount, String qSearch) {

        when:
        Response resp = get("${getPath(entity)}?q=${qSearch}")
        assert resp.code() == HttpStatus.OK.value()
        assert resp.successful
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.records == qCount

        where:

        entity       | qCount | qSearch
        'rally/org'  | 1      | 'Org11'
        'rally/user' | 1      | 'admin'
        //'location' | 1      | 'Denver'

    }
}
