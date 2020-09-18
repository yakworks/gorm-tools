package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiTestTrait
import grails.testing.mixin.integration.Integration
// import grails.transaction.Rollback
import org.springframework.test.annotation.Rollback

import spock.lang.IgnoreRest

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

@Integration
@Rollback
class BookRestApiSpec extends GebSpec implements RestApiTestTrait {

    String path = "api/book"
    Map postData = [name: "foo"]
    Map putData = [name: "updated foo"]

    void "get index list"() {
        when:
        def pageMap = testList()
        def data = pageMap.data

        then:
        data.size() == 5
        Map book = data[0] as Map
        Book.includes.containsAll(book.keySet())
        book.keySet().containsAll(Book.includes)
    }

    void "get picklist"() {
        when:
        def pageMap = testPickList()
        def data = pageMap.data

        then:
        data.size() == 5
        Map book = data[0] as Map
        book.keySet().size() == 2 //should be the id and name
        book['id'] == 1
        book['name']
    }

    void "test qSearch"() {
        expect:
        testList('galt').data.size() == 3
        testList('flubber').data.size() == 0

        def pmap = testList('shrugged1')
        pmap.data.size() == 1
        pmap.data[0].description == 'Shrugged1'

        //test on picklist
        testPickList('galt').data.size() == 3
        testPickList('flubber').data.size() == 0
    }

    void "test get"() {
        expect:
        def response = restBuilder.get("$resourcePath/1")
        response.status == OK.value()
    }

    void "exercise api"() {
        expect:
        testGet()
        testPost()
        testPut()
        testDelete()
    }

}
