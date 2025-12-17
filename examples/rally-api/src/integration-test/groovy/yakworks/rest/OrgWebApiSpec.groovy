package yakworks.rest


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import gorm.tools.beans.Pager
import gorm.tools.transaction.WithTrx
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Specification

import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.rest.client.WebClientTrait
import static yakworks.json.groovy.JsonEngine.parseJson

/**
 * POC that Uses WebClient for testing instead of OkHttp
 * Requires
 */
@Integration
class OrgWebApiSpec extends Specification implements WebClientTrait, WithTrx {

    String path = "/api/rally/org"
    String contactApiPath = "/api/rally/contact"
    String locationApiPath = "/api/rally/location"

    Map postData = [num:'foo1', name: "foo", type: 'Customer']
    Map putData = [name: "updated foo1"]

    def setup(){
        login()
    }

    void "get picklist"() {
        when:
        ResponseEntity resp = get("$path/picklist?q=*")
        Map body = resp.body

        then:
        resp.statusCode == HttpStatus.OK
        body.data.size() == 50
        Map book = body.data[0] as Map
        book.keySet().size() == 3 //should be the id and name and num
        book['id'] == 1
        book['name']
    }

    void "get picklist mono pager"() {
        when:
        Pager pager = getBody("$path/picklist?q=*", Pager)

        then:
        pager.data.size() == 50
        Map book = pager.data[0] as Map
        book.keySet().size() == 3 //should be the id and name and num
        book['id'] == 1
        book['name']
    }

    void "smoke test csv"() {
        when:
        ResponseEntity resp = getBytes("${path}?q=*&format=csv")
        // Map body = bodyToMap(resp)

        then:
        resp.statusCode == HttpStatus.OK

    }

    void "smoke test xlsx"() {
        when:
        ResponseEntity resp = getBytes("${path}?q=*&format=xlsx")

        then:
        resp.statusCode == HttpStatus.OK

    }

    void "test qSearch"() {
        when:
        //gets all that start with org 2
        Map body  = getBody("$path?qSearch=org2")

        then: "should be 10 of them"
        body.data.size() == 10

        when:
        body  = getBody("$path?qSearch=flubber")

        then:
        body.data.size() == 0

        when: 'num search'
        body  = getBody("$path?qSearch=11")

        then:
        body.data.size() == 1
        body.data[0].num == '11'

        when: 'picklist search'
        body  = getBody("$path/picklist?qSearch=org12")

        then:
        body.data.size() == 1

    }

    void "test q"() {
        when:
        String euri = encodeQueryParam('{name: "Org20"}')
        ResponseEntity resp = get("${path}?q=$euri")
        Map body = resp.getBody()

        then:
        body.data.size() == 1
        body.data[0].name == "Org20"
    }

    void "test sorting"() {
        when: "sort asc"
        def resp = get("${path}?q=*&sort=id&order=asc")
        Map body = resp.getBody()

        then:
        resp.statusCode == HttpStatus.OK
        //the first id should be less than the second
        body.data[0].id < body.data[1].id

        when: "sort desc"
        resp = get("${path}?q=*&sort=id&order=desc")
        body = resp.getBody()

        then: "The response is correct"
        resp.statusCode == HttpStatus.OK
        //the first id should be less than the second
        body.data[0].id > body.data[1].id
    }

    void "test get"() {
        when:
        def resp = get("$path/1")
        Map body = resp.body

        then:
        resp.statusCode == HttpStatus.OK
        body.id
        body.name == 'Org1'
    }

    void "testing post"() {
        when:
        ResponseEntity resp = post(path, [num: "foobie123", name: "foobie", type: "Customer"])
        Map body = resp.body

        then:
        resp.statusCode == HttpStatus.CREATED
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
        Map source = [source: "source", sourceType: "ERP", sourceId: "1"]

        List<Map> contacts = [
            [name: "C1", firstName: "C1", phones: phones, emails:emails, source: source],
            [name: "C2", firstName: "C2"]
        ]
        ResponseEntity resp = post(path, [num: "111", name: "Org-with-contact", type: "Customer", contacts:contacts])

        Map body = resp.body
        def orgId = body.id

        then:
        resp.statusCode == HttpStatus.CREATED
        body.id
        body.name == 'Org-with-contact'

        when: "Verify contacts are created"
        String euri = encodeQueryParam("{orgId:$orgId}")
        ResponseEntity contactResp = get("${contactApiPath}?q=$euri")
        // ResponseEntity contactResp = get("${contactApiPath}?q={orgId:$orgId}")
        Map contactBody = contactResp.body

        then: "Verify locations are created"
        contactResp.statusCode == HttpStatus.OK
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
        c.source
        c.source.source == "source"

        // cleanup:
        // delete(path, orgId)
        // delete(contactApiPath, contactBody.data[0].id)
        // delete(contactApiPath, contactBody.data[1].id)
    }

    void "test post with locations"() {
        when:
        Map primaryLocation =  [name: "P1", street1: "P1", city:"P1", state: "P1"]
        List<Map> locations = [
            [name: "L1", street1: "L1", city:"L1", state: "L1"],
            [name: "L2", street1: "L2", city:"L2", state: "L2"],
        ]

        def resp = post(path, [num: "111222", name: "Org-with-location", type: "Customer", locations:locations, location:primaryLocation])

        Map body = resp.body
        def orgId = body.id

        then:
        resp.statusCode == HttpStatus.CREATED
        body.id
        body.name == 'Org-with-location'
        body.location.id != null

        when: "Verify locations are created"
        String euri = encodeQueryParam("{orgId:$orgId}")
        ResponseEntity locationResp = get("${locationApiPath}?q=$euri")
        // def locationResp = get("$locationApiPath?q={orgId:$orgId}")
        Map locationBody = locationResp.body

        then:
        locationResp.statusCode == HttpStatus.OK
        locationBody.data.size() == 3
        locationBody.data[0].id == body.location.id
        locationBody.data[0].name == "P1"
        locationBody.data[0].street1 == "P1"
        locationBody.data[1].name == "L1"
        locationBody.data[1].street1 == "L1"
        locationBody.data[2].name == "L2"
        locationBody.data[2].street1 == "L2"

        delete(path, orgId)
    }

    void "testing post bad data"() {
        when:
        post(path, [name: "foobie", type: "Customer"])

        then: //web client throws exception when encounters 422, need to get response json from exception
        WebClientResponseException.UnprocessableEntity e = thrown()
        e.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
        e.message.contains "422 Unprocessable Entity"

        when:
        String errorResponse = e.getResponseBodyAsString()
        Map body = parseJson(errorResponse)

        then:
        body.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
        body.title == 'Org Validation Error(s)'
        body.errors[0].code == 'NotNull'
        body.errors[0].message == 'must not be null'
    }

    void "testing put"() {
        when:
        def resp = put(path, [name: "9Galt"], 66)

        Map body = resp.body

        then:
        resp.statusCode == HttpStatus.OK
        body.id
        body.name == '9Galt'

    }

    void "test post with tags"() {
        when: "Create a test tag"
        Tag tag1 = Tag.create(code: 'T11', entityName: 'Customer')

        then:
        tag1

        when: "Create customer with tags"
        def resp = post(path, [num:"C11", name:"C11", type: 'Customer', tags:[[id:tag1.id]]])
        Map body = resp.body

        then: "Verify org tags created"
        // resp.code() == 201
        //do an if then here so we get better display on failure
        if(resp.statusCode !=  HttpStatus.CREATED){
            assert body == [WTF: "Work That Failed"]
        }
        body.id
        body.tags[0].id == tag1.id

        cleanup:
        withTrx {
            Org.repo.removeById(body.id as Long)
        }
    }
}
