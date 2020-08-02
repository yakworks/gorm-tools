/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.testing

import groovy.transform.CompileDynamic

import geb.spock.GebSpec
import gorm.tools.testing.TestTools

import static grails.web.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.*

@SuppressWarnings(['NoDef', 'AbstractClassWithoutAbstractMethod', 'Indentation', 'JUnitTestMethodWithoutAssert'])
@CompileDynamic
abstract class RestApiFuncSpec extends GebSpec implements RestApiTestTrait {
    boolean vndHeaderOnError = false

    // TODO: add ability to pass input and output data to be able to test overidden repos
    abstract Map getInvalidData()

    abstract Map getPostData()

    abstract Map getPutData()

    List<String> getExcludes() {
        []
    }

    void "get index list"() {
        expect:
        testList()
    }

    void "get test"() {
        expect:
        testGet()
    }

    void "post test"() {
        expect:
        testPost()
    }
    //
    void "put test"() {
        expect:
        testPut()
    }

    void "delete test"() {
        expect:
        testDelete()
    }

    void testList() {
        def response = post_a_valid_resource()
        response = restBuilder.get(resourcePath)
        assert response.status == OK.value()
        assert response.json.size() >= 0 // == []
    }

    void testPost() {
        // "The save action is executed with no content"
        def response = restBuilder.post(resourcePath)
        // "The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)

        // "The save action is executed with invalid data"
        response = restBuilder.post(resourcePath) {
            json invalidData
        }
        // "The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)

        // "The save action is executed with valid data"
        response = restBuilder.post(resourcePath) {
            json postData
        }

        //"The response is correct"
        assert response.status == CREATED.value()
        verifyHeaders(response)
        //response.json.id
        assert TestTools.mapContains(response.json, postData, excludes)
        //Project.count() > 1// == 1
        def rget = restBuilder.get("$resourcePath/${response.json.id}")
        assert TestTools.mapContains(rget.json, postData, excludes)
    }

    void testPut() {
        def response = post_a_valid_resource()

        // "The update action is called with invalid data"
        def goodId = response.json.id
        def response2 = restBuilder.put("$resourcePath/$goodId") {
            json invalidData
        }

        // "The response is invalid"
        verify_UNPROCESSABLE_ENTITY(response2)

        // "The update action is called with valid data"
        goodId = response.json.id
        response = restBuilder.put("$resourcePath/$goodId") {
            json putData
        }

        // "The response is correct"
        assert response.status == OK.value()
        //response.json
        assert TestTools.mapContains(response.json, putData, excludes)
        //get it and make sure
        def rget = restBuilder.get("$resourcePath/$goodId")
        assert TestTools.mapContains(rget.json, putData, excludes)
        // subsetEquals(putData, rget.json, excludes)

    }

    void testGet() {
        // "The save action is executed with valid data"
        def response = post_a_valid_resource()

        // "When the show action is called to retrieve a resource"
        def id = response.json.id
        response = restBuilder.get("$resourcePath/$id")

        // "The response is correct"
        assert response.status == OK.value()
        assert response.json.id == id
    }

    void testDelete() {
        // "The save action is executed with valid data"
        def response = post_a_valid_resource()
        def id = response.json.id

        // "When the delete action is executed on an unknown instance"
        response = restBuilder.delete("$resourcePath/99999")

        // "The response is bad"
        response.status == NOT_FOUND.value()

        // when: "When the delete action is executed on an existing instance"
        response = restBuilder.delete("$resourcePath/$id")

        // then: "The response is correct"
        assert response.status == NO_CONTENT.value()
    }

    def post_a_valid_resource() {
        def response = restBuilder.post(resourcePath) {
            json postData
        }
        verifyHeaders(response)
        // println "response.json ${response.json}"
        assert response.status == CREATED.value()
        assert response.json.id
        return response
    }

    def verifyHeaders(Object response) {
        //assert response.headers.getFirst(CONTENT_TYPE) == 'application/json;charset=UTF-8'
        //assert response.headers.getFirst(HttpHeaders.LOCATION) == "$resourcePath/${response.json.id}"
        true
    }

    def verify_UNPROCESSABLE_ENTITY(Object response) {
        assert response.status == UNPROCESSABLE_ENTITY.value()
        if (vndHeaderOnError) {
            assert response.headers.getFirst(CONTENT_TYPE) == 'application/vnd.error;charset=UTF-8'
        }
        true
    }

}
