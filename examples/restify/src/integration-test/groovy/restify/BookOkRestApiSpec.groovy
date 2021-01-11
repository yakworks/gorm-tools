package restify

import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Response
import spock.lang.Specification

@Integration
class BookOkRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/book"
    Map postData = [name: "fountain"]
    Map putData = [name: "updated fountain"]

    void "get index list"() {
        when:
        Response resp = get(path)
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 5
        Map book = body.data[0] as Map
        Book.includes.containsAll(book.keySet())
        book.keySet().containsAll(Book.includes)
    }

    void "get picklist"() {
        when:
        Response resp = get("$path/picklist")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 5
        Map book = body.data[0] as Map
        book.keySet().size() == 2 //should be the id and name
        book['id'] == 1
        book['name']
    }

    void "test qSearch"() {
        when:
        Response resp = get("$path?q=galt")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 3

        when:
        resp = get("$path?q=flubber")
        body = bodyToMap(resp)

        then:
        body.data.size() == 0

        when: 'description search'
        resp = get("$path?q=shrugged1")
        body = bodyToMap(resp)

        then:
        body.data.size() == 1
        body.data[0].description == 'Shrugged1'

        when: 'picklist search'
        resp = get("$path/picklist?q=galt")
        body = bodyToMap(resp)

        then:
        body.data.size() == 3

    }

    void "test q"() {
        when:
        String q = '{description: "Shrugged1"}'
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getUrl(path)).newBuilder()
        urlBuilder.addQueryParameter("q", q)
        def resp = get(urlBuilder.build().toString())
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 1
        body.data[0].description == "Shrugged1"
    }

    void "test sorting"() {
        when: "sort asc"
        def resp = get("${path}?sort=id&order=asc")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        //the first id should be less than the second
        body.data[0].id < body.data[1].id

        when: "sort desc"
        resp = get("${path}?sort=id&order=desc")
        body = bodyToMap(resp)

        then: "The response is correct"
        resp.code() == HttpStatus.OK.value()
        //the first id should be less than the second
        body.data[0].id > body.data[1].id
    }

    void "test get"() {
        when:
        def resp = get("$path/1")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        body.name.toString().startsWith('Galt')
    }

    void "testing post"() {
        when:
        Response resp = post(path, [name: "foobie"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'foobie'
        delete(path, body.id)
    }

    void "testing post bad data"() {
        when:
        Response resp = post(path, [desc: "foobie"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.total == 1
        body.message == 'Book validation errors'
        body.errors.find{ it.field == 'name' }.message == 'Property [name] of class [class restify.Book] cannot be null'
    }

    void "testing put"() {
        when:
        Response resp = put(path, [name: "9Galt"], 1)

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        body.name == '9Galt'

    }

    void "testing massUpdate"() {
        when:
        Response resp = post("$path/massUpdate", [ids: [1, 2], data: [name: "mass Updated"]])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data[0].name == 'mass Updated'
        body.data[1].name == 'mass Updated'

    }

}
