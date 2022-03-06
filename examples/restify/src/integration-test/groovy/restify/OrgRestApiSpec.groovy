package restify

import grails.gorm.transactions.Rollback
import org.springframework.http.HttpStatus

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Response
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag

@Integration
class OrgRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/org"
    String contactApiPath = "/api/rally/contact"
    String locationApiPath = "/api/rally/location"

    Map postData = [num:'foo1', name: "foo", type: 'Customer']
    Map putData = [name: "updated foo1"]

    void "get picklist"() {
        when:
        Response resp = get("$path/picklist")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 50
        Map book = body.data[0] as Map
        book.keySet().size() == 3 //should be the id and name and num
        book['id'] == 1
        book['name']
    }

    void "test qSearch"() {
        when:
        Response resp = get("$path?q=org2")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 11

        when:
        resp = get("$path?q=flubber")
        body = bodyToMap(resp)

        then:
        body.data.size() == 0

        when: 'num search'
        resp = get("$path?q=11")
        body = bodyToMap(resp)

        then:
        body.data.size() == 1
        body.data[0].num == '11'

        when: 'picklist search'
        resp = get("$path/picklist?q=org12")
        body = bodyToMap(resp)

        then:
        body.data.size() == 1

    }

    void "test q"() {
        when:
        String q = '{name: "Org20"}'
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getUrl(path)).newBuilder()
        urlBuilder.addQueryParameter("q", q)
        def resp = get(urlBuilder.build().toString())
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 1
        body.data[0].name == "Org20"
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
        body.name == 'Org1'
    }

    void "testing post"() {
        when:
        Response resp = post(path, [num: "foobie123", name: "foobie", type: "Customer"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'foobie'

        cleanup:
        delete(path, body.id)
    }

    @Rollback
    void "testing post with contacts"() {
        when:

        List<Map> phones = [[kind: "kind", num: "123"]]
        List<Map> emails = [[kind: "kind", address: "test@9ci.com"]]
        List<Map> sources = [[source: "source", sourceType: "RestApi", sourceId: "1"]]

        List<Map> contacts = [
            [name: "C1", firstName: "C1", phones: phones, emails:emails, sources: sources],
            [name: "C2", firstName: "C2"],
        ]
        Response resp = post(path, [num: "111", name: "Org-with-contact", type: "Customer", contacts:contacts])

        Map body = bodyToMap(resp)
        def orgId = body.id

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'Org-with-contact'

        when: "Verify contacts are created"
        Response contactResp = get("$contactApiPath?q={orgId:$orgId}")
        Map contactBody = bodyToMap(contactResp)

        then: "Verify locations are created"
        contactResp.code() == HttpStatus.OK.value()
        contactBody.data.size() == 2
        contactBody.data[0].name == "C1"
        contactBody.data[0].firstName == "C1"
        contactBody.data[1].name == "C2"
        contactBody.data[1].firstName == "C2"

        when: "Verify contact associations"
        Contact c = Contact.get(contactBody.data[0].id)

        then:
        c != null
        c.phones.size() == 1
        c.phones[0].num == "123"
        c.emails.size() == 1
        c.emails[0].address == "test@9ci.com"
        c.sources.size() == 1
        c.sources[0].source == "source"

        cleanup:
        delete(path, orgId)
        delete(contactApiPath, contactBody.data[0].id)
        delete(contactApiPath, contactBody.data[1].id)
    }

    void "test post with locations"() {
        when:
        Map primaryLocation =  [name: "P1", street1: "P1", city:"P1", state: "P1"]
        List<Map> locations = [
            [name: "L1", street1: "L1", city:"L1", state: "L1"],
            [name: "L2", street1: "L2", city:"L2", state: "L2"],
        ]

        Response resp = post(path, [num: "111", name: "Org-with-location", type: "Customer", locations:locations, location:primaryLocation])

        Map body = bodyToMap(resp)
        def orgId = body.id

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'Org-with-location'
        body.location.id != null

        when: "Verify locations are created"
        Response locationResp = get("$locationApiPath?q={orgId:$orgId}")
        Map locationBody = bodyToMap(locationResp)

        then:
        locationResp.code() == HttpStatus.OK.value()
        locationBody.data.size() == 3
        locationBody.data[0].id == body.location.id
        locationBody.data[0].name == "P1"
        locationBody.data[0].street1 == "P1"
        locationBody.data[1].name == "L1"
        locationBody.data[1].street1 == "L1"
        locationBody.data[2].name == "L2"
        locationBody.data[2].street1 == "L2"

        delete(path, orgId)
        delete(locationApiPath, locationBody.data[0].id)
        delete(locationApiPath, locationBody.data[1].id)
        delete(locationApiPath, locationBody.data[2].id)
    }

    void "testing post bad data"() {
        when:
        Response resp = post(path, [name: "foobie", type: "Customer"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == 'Org Validation Error(s)'
        //body.detail == "Org Validation Error(s)"
        body.errors[0].code == 'NotNull'
        body.errors[0].message == 'must not be null'
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

    void "test post with tags"() {
        when: "Create a test tag"
        Tag tag1 = Tag.create(code: 'T1', entityName: 'Customer')

        then:
        tag1

        when: "Create customer with tags"
        Response resp = post(path, [num:"C1", name:"C1", type: 'Customer', tags:[[id:tag1.id]]])
        Map body = bodyToMap(resp)

        then: "Verify org tags created"
        // resp.code() == 201
        //do an if then here so we get better display on failure
        if(resp.code() !=  201){
            assert body == [WTF: "Work That Failed"]
        }
        body.tags[0].id == tag1.id

        cleanup:
        Org.removeById(body.id as Long)

    }
}
