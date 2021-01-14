package restify

import org.springframework.http.HttpStatus

import yakworks.commons.map.Maps
import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Sanity checks to hit the main endpoints. KISS, keep it simple
 * any special testing logic for an entity should be in its own test
 */
@Integration
class ExerciseRestApiSpec extends Specification implements OkHttpRestTrait {

    String getPath(String entity) { "/api/${entity}" }

    void "get index list"() {
        when:
        Response resp = get(getPath('task'))
        assert resp.code() == HttpStatus.OK.value()
        Map pageMap = bodyToMap(resp)

        then:
        pageMap.data.size() == 20

    }

    @Unroll
    def "LIST get test #entity"(String entity, Integer qCount) {

        when:
        Response resp = get("${getPath(entity)}")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.records == qCount

        where:

        entity      | qCount
        'book'      | 5
        'org'       | 100
        'location'  | 100
        'project'   | 50
        'task'      | 100

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
        'book'      | [name:'Galt123']
        'org'       | [num:'foo1', name: "foo", type: [id: 1]]
        'location'  | [city: "Chicago"]
        'project'   | [name: "project", num: "x123"]
        'task'      | [name: "task", project: [id: 1]]
         //'user'      | [name: 'taggy', entityName: 'Customer']
    }

    @Unroll
    def "PUT update: #entity/#id"(String entity, Long id, String prop, String val) {

        when:
        def putData = [(prop): val]
        Response resp = put("${getPath(entity)}/$id", putData)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body[prop] == val

        where:

        entity     | id | prop   | val
        'book'     | 1  | 'name' | 'Galt234'
        'org'      | 1  | 'num'  | 'foo123'
        'location' | 1  | 'city' | 'Denver'
        'project'  | 1  | 'name' | 'project123'
        'task'     | 1000  | 'name' | 'task123'

    }


    @Unroll
    def "q text search: #entity?q=#qSearch"(String entity, Integer qCount, String qSearch) {

        when:
        Response resp = get("${getPath(entity)}?q=${qSearch}")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.records == qCount

        where:

        entity     | qCount | qSearch
        'book'     | 3      | 'galt'
        'org'      | 1      | 'foo123'
        'location' | 1      | 'Denver'
        'project'  | 1      | 'project123'
        'task'     | 1      | 'task123'

    }
}
