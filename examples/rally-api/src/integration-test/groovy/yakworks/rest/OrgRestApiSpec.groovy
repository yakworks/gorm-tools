package yakworks.rest

import gorm.tools.transaction.WithTrx
import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import okhttp3.Request
import okhttp3.RequestBody
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus

import spock.lang.Ignore
import spock.lang.IgnoreRest
import yakworks.rally.orgs.model.Location
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Response
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import static yakworks.etl.excel.ExcelUtils.getHeader

@Integration
class OrgRestApiSpec extends Specification implements OkHttpRestTrait, WithTrx {

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
        Response resp = get("$path/picklist?q=*")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 50
        Map book = body.data[0] as Map
        book.keySet().size() == 3 //should be the id and name and num
        book['id'] == 1
        book['name']
    }

    void "test csv"() {
        when:
        Response resp = get("${path}?q=*&format=csv")
        // Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()

    }

    void "test qSearch"() {
        when:
        Response resp = get("$path?qSearch=org2")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.data.size() == 10

        when:
        resp = get("$path?qSearch=flubber")
        body = bodyToMap(resp)

        then:
        body.data.size() == 0

        when: 'num search'
        resp = get("$path?qSearch=11")
        body = bodyToMap(resp)

        then:
        body.data.size() == 1
        body.data[0].num == '11'

        when: 'picklist search'
        resp = get("$path/picklist?qSearch=org12")
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

    void "test q used like qSearch"() {
        when:
        def resp = get("$path/picklist?q=foo")
        Map body = bodyToMap(resp)

        then:
        resp.code == 400
        !body.ok
        body.title == "Invalid Query"
        body.code == "error.query.invalid"
        body.detail.contains "Invalid JSON"
    }

    void "test invalid q"() {
        when:
        String q = '({name: "Org20"})'
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getUrl(path)).newBuilder()
        urlBuilder.addQueryParameter("q", q)
        def resp = get(urlBuilder.build().toString())
        Map body = bodyToMap(resp)

        then:
        resp.code == 400
        !body.ok
        body.title == "Invalid Query"
        body.code == "error.query.invalid"
        body.detail.contains "Invalid JSON"
    }


    void "default sort by id"() {

        when: "default sort by id asc"
        def resp = get("${path}?q=*")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body
        body.data
        body.data.eachWithIndex{ def entry, int i ->
            if(i > 0) {
                assert entry['id'] > body.data[i - 1]['id']
            }
        }

        when: 'default sort should not apply if $sort is in q'
        String q = '{inactive: true, $sort: {id:"desc"}}' //This should apply sort id:desc, instead of default id:asc
        resp = get("${path}?q=$q")
        body = bodyToMap(resp)

        then: 'sorted as per $sort from q'
        resp.code() == HttpStatus.OK.value()
        body
        body.data
        body.data.eachWithIndex{ def entry, int i ->
            if(i > 0) {
                assert entry['id'] < body.data[i - 1]['id']
            }
        }
    }

    void "default sort by id should not apply with projections"() {
        when: "there's a projection without id column"
        def resp = get("${path}?q=*&projections="+'flex.num1:"sum",type:"group"')
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body
        body.records == 5 //RallySeed creates orgs with 5 different org types, so one record for each group
        body.data.size() == 5
        body.data[0].type.name != null
        body.data[0].flex.num1 != null
    }

    void "test explicit sort"() {
        when: "sort asc"
        def resp = get("${path}?q=*&sort=id&order=asc")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        //the first id should be less than the second
        body.data[0].id < body.data[1].id

        when: "sort desc"
        resp = get("${path}?q=*&sort=id&order=desc")
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

    void "testing UPSERT insert"() {
        when:
        Response resp = post("$path/upsert", [num: "upsert1", name: "upsert1", type: "Customer"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'upsert1'

        cleanup:
        delete(path, body.id)
    }

    void "testing UPSERT update"() {
        when:
        Response resp = post("$path/upsert", [id: 89, name: "upsert2", type: "Customer"])

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        body.name == 'upsert2'

    }

    @Rollback
    void "testing post with contacts"() {
        when:

        List<Map> phones = [[kind: "kind", num: "123"]]
        List<Map> emails = [[kind: "kind", address: "test@9ci.com"]]
        Map source = [source: "source", sourceType: "ERP", sourceId: "11"]

        List<Map> contacts = [
            [name: "C1", firstName: "C1", phones: phones, emails:emails, source: source],
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
        c.source
        c.source.source == "source"

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

    @Transactional
    void "testing put"() {
        setup:
        int countBefore = Location.count()
        Location existing = Org.get(67).location

        when:
        Response resp = put(path, [name: "9Galt", location:[city:"test"], locations:[[:]]], 67)
        Map body = bodyToMap(resp)
        Location updated = Org.get(67).location
        int countAfter = Location.count()

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        body.name == '9Galt'

        and:
        countBefore == countAfter
        existing.id == updated.id
        updated.city == "test"
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
        withTrx {
            Org.repo.removeById(body.id as Long)
        }
    }

    void "malformed json in request"() {
        setup:
        Request request = getRequestBuilder(path)
            .method("POST", RequestBody.create('{"num":"C1", name:"C1"}', null))
            .build()

        when:
        Response resp = getHttpClient().newCall(request).execute()
        Map body = bodyToMap(resp)

        then:
        resp.code() == 400
        body.status == 400
        !body.ok
        body.code == "error.data.problem"
        body.detail.contains "expecting '}'"
    }

    //XXX @SUD turn back on when secureCrudApi is sorted out
    // is this the only test we have?
    @Ignore
    void "test readonly operation"() {
        setup:
        OkAuth.TOKEN = null
        login("readonly", "123")

        when:
        String q = '{name: "Org20"}'
        def resp = post(path,  [num:"C1", name:"C1", type: 'Customer'])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.UNAUTHORIZED.value()
        body
        !body.ok
        body.code == "error.unauthorized"
        body.title == 'Unauthorized'
        body.detail == 'Access Denied'

        cleanup:
        OkAuth.TOKEN = null
    }
}
