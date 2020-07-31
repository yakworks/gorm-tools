package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiTestTrait
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import taskify.Project

import static grails.web.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.*

@Integration
class ProjectControllerSpec extends GebSpec implements RestApiTestTrait {

    Class<Project> domainClass = Project
    boolean vndHeaderOnError = false

    String getResourcePath() {
        println "${baseUrl}api/project"
        "${baseUrl}api/project"
    }

    List<String> getExcludes() { ["activateDate"] }

    //data to force a post or patch failure
    Map getInsertData() {
        [name: "project", num: "x123", inactive: true, billable: true, activateDate: new Date(), endDate: new Date(), startDate: new Date()]
    }

    Map getInvalidData() { ["name": null] }

    Map getUpdateData() {
        [name: "project", num: "test", inactive: true, billable: true]
    }

    // @IgnoreRest
    void test_get_index() {
        // BootStrap should have loaded up projects already
        when: "The default index action is requested"
        def response = restBuilder.get(resourcePath)

        then: "The response is correct"
        response.status == OK.value()
        def resJson = response.json
        println "response.json ${resJson}"
        resJson.data.size() == 10
        resJson.page == 1
        resJson.total >= 1
        //check that first item in data list has the fields from listIncludes
        resJson.data[0] == [id:1 , name: "Fooinator-1", num: "1", billable:true]
    }

    // @IgnoreRest
    void test_pick_list() {
        // BootStrap should have loaded up projects already
        when: "The default index action is requested"
        def response = restBuilder.get("$resourcePath/pickList")

        then: "The response is correct"
        response.status == OK.value()
        def resJson = response.json
        println "response.json ${resJson}"
        resJson.data.size() == 10
        resJson.page == 1
        resJson.total >= 1
        //check that first item in data list has the fields from listIncludes
        resJson.data[0] == [id:1 , name: "Fooinator-1", num: "1"]
    }

    @Ignore
    void test_save_post() {
        given:
        def response
        Map data = insertData
        data.num = "foo"
        when: "The save action is executed with no content"
        response = restBuilder.post(resourcePath)

        then: "The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)

        when: "The save action is executed with invalid data"
        response = restBuilder.post(resourcePath) {
            json invalidData
        }
        then: "The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)

        when: "The save action is executed with valid data"
        response = restBuilder.post(resourcePath) {
            json insertData
        }

        then: "The response is correct"
        response.status == CREATED.value()
        verifyHeaders(response)
        //response.json.id
        subsetEquals(data, response.json as Map, excludes)
        //Project.count() > 1// == 1
        def rget = restBuilder.get("$resourcePath/${response.json.id}")
        subsetEquals(data, rget.json as Map, excludes)
    }

    void test_update_put() {
        given:
        def response = post_a_valid_resource()

        when: "The update action is called with invalid data"
        def goodId = response.json.id
        def response2 = restBuilder.put("$resourcePath/$goodId") {
            json invalidData
        }

        then: "The response is invalid"
        verify_UNPROCESSABLE_ENTITY(response2)

        when: "The update action is called with valid data"
        goodId = response.json.id
        response = restBuilder.put("$resourcePath/$goodId") {
            json updateData
        }

        then: "The response is correct"
        response.status == OK.value()
        //response.json
        subsetEquals(updateData, response.json, excludes)
        //get it and make sure
        def rget = restBuilder.get("$resourcePath/$goodId")
        subsetEquals(updateData, rget.json, excludes)

    }

    // @IgnoreRest
    void test_show_get() {
        when: "When the show action is called to retrieve a resource"
        def response = restBuilder.get("$resourcePath/1")

        then: "The response is correct"
        response.status == OK.value()
        response.json == [activateDate:'2020-01-01T00:00:00Z', inactive:false, comments:null, endDate:null,
                          num:'1', name:'Fooinator-1', id:1, billable:true, startDate:'2020-01-01']
    }

    void test_delete() {
        given: "The save action is executed with valid data"
        def response = post_a_valid_resource()
        def id = response.json.id

        when: "When the delete action is executed on an unknown instance"
        response = restBuilder.delete("$resourcePath/99999")

        then: "The response is bad"
        response.status == NOT_FOUND.value()

        when: "When the delete action is executed on an existing instance"
        response = restBuilder.delete("$resourcePath/$id")

        then: "The response is correct"
        response.status == NO_CONTENT.value()
    }

    def post_a_valid_resource() {
        def response = restBuilder.post(resourcePath) {
            json insertData
        }
        // println response.body
        // println response
        verifyHeaders(response)
        assert response.status == CREATED.value()
        assert response.json.id
        return response
    }

    def verifyHeaders(response) {
        //assert response.headers.getFirst(CONTENT_TYPE) == 'application/json;charset=UTF-8'
        //assert response.headers.getFirst(HttpHeaders.LOCATION) == "$resourcePath/${response.json.id}"
        true
    }

    def verify_UNPROCESSABLE_ENTITY(response) {
        assert response.status == UNPROCESSABLE_ENTITY.value()
        if (vndHeaderOnError) {
            assert response.headers.getFirst(CONTENT_TYPE) == 'application/vnd.error;charset=UTF-8'
        }
        true
    }


}
