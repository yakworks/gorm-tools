package restify

import geb.spock.GebSpec
import gorm.tools.rest.client.RestApiTestTrait
import gorm.tools.testing.TestTools
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import yakworks.taskify.domain.Project

import static grails.web.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.*

/**
 * manual testing example
 */
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
        [name: "project 123", num: "test", inactive: true, billable: true]
    }

    void test_get_index() {
        // BootStrap should have loaded up projects already
        when: "The default index action is requested"
        def response = restBuilder.get(resourcePath)

        then: "The response is correct"
        response.status == OK.value()
        def resJson = response.json
        println "response.json ${resJson}"
        resJson.data.size() == 20
        resJson.page == 1
        resJson.total >= 1
        //check that first item in data list has the fields from listIncludes
        resJson.data[0] == [id:1 , name: "Fooinator-1", num: "1", billable:true]
    }

    void test_sort_list() {
        // BootStrap should have loaded up projects already
        when: "sort asc"
        def response = restBuilder.get("${baseUrl}api/project?sort=id&order=asc")
        def data = response.json.data

        then: "The response is correct"
        response.status == OK.value()
        //the first id should be less than the second
        data[0].id < data[1].id

        when: "sort desc"
        response = restBuilder.get("${baseUrl}api/project?sort=id&order=desc")
        data = response.json.data

        then: "The response is correct"
        response.status == OK.value()
        //the first id should be greater  than the second
        data[0].id > data[1].id
    }

    void test_pick_list() {
        // BootStrap should have loaded up projects already
        when: "The default index action is requested"
        def response = restBuilder.get("$resourcePath/picklist")

        then: "The response is correct"
        response.status == OK.value()
        def resJson = response.json
        println "response.json ${resJson}"
        resJson.data.size() == 20
        resJson.page == 1
        resJson.total >= 1
        //check that first item in data list has the fields from listIncludes
        resJson.data[0] == [id:1 , name: "Fooinator-1", num: "1"]
    }

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
        response.json.total == 2
        response.json.message == 'yakworks.taskify.domain.Project save failed'
        response.json.errors[0].message == "Property [name] of class [class yakworks.taskify.domain.Project] cannot be null"
        response.json.errors[0].field == "name"
        response.json.errors[1].message == "Property [num] of class [class yakworks.taskify.domain.Project] cannot be null"
        response.json.errors[1].field == "num"

        /*when: "The save action is executed with valid data"
        response = restBuilder.post(resourcePath) {
            json insertData
        }

        then: "The response is correct"
        response.status == CREATED.value()
        verifyHeaders(response)
        //response.json.id
        TestTools.mapContains(response.json, data, excludes)
        //Project.count() > 1// == 1
        def rget = restBuilder.get("$resourcePath/${response.json.id}")
        TestTools.mapContains(rget.json, data, excludes)*/
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
        TestTools.mapContains(response.json, updateData, excludes)
        //get it and make sure
        def rget = restBuilder.get("$resourcePath/$goodId")
        TestTools.mapContains(rget.json, updateData, excludes)
    }

    void test_show_get() {
        when: "When the show action is called to retrieve a resource"
        def response = restBuilder.get("$resourcePath/1")

        then: "The response is correct"
        response.status == OK.value()
        response.json == [activateDate:'2020-01-01T00:00:00Z', inactive:false, comments:null, endDate:null,
                          num:'1', name:'Fooinator-1', id:1, billable:true, startDate:'2020-01-01', version: 0]
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
