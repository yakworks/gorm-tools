/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.testing

import groovy.transform.CompileDynamic

import gorm.tools.testing.TestTools
import grails.plugins.rest.client.RestBuilder

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

//@CompileStatic
@CompileDynamic
trait RestApiTestTrait {

    //private static GrailsApplication _grailsApplication
    //private static Object _servletContext

    String getResourcePath() { "${baseUrl}/${path}" }

    RestBuilder getRestBuilder() {
        new RestBuilder()
    }

    List<String> getExcludes() { [] }

    Map getInvalidData() { return [:] }

    def testList(String qSearch) {
        qSearch = qSearch ? "?q=${qSearch}" : ""
        def res = restBuilder.get("${getResourcePath()}${qSearch}")
        assert res.status == OK.value()
        def pageMap = res.json
        return pageMap
    }

    def testPickList(String qSearch) {
        qSearch = qSearch ? "?q=${qSearch}" : ""
        def res = restBuilder.get("${getResourcePath()}/pickList${qSearch}")
        assert res.status == OK.value()
        def pageMap = res.json
        return pageMap
    }

    def testPost() {
        // "The save action is executed with valid data"
        def response = restBuilder.post(getResourcePath()) {
            json getPostData()
        }

        //"The response is correct"
        assert response.status == CREATED.value()
        verifyHeaders(response)
        //response.json.id
        assert TestTools.mapContains(response.json, postData, excludes)
        //Project.count() > 1// == 1
        def rget = restBuilder.get("${getResourcePath()}/${response.json.id}")
        assert TestTools.mapContains(rget.json, postData, excludes)
        return rget
    }

    def testPostInvalid() {
        // "The save action is executed with invalid data"
        def response = restBuilder.post(getResourcePath()) {
            json getInvalidData()
        }
        // "The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)
    }


    def testPut() {
        def response = post_a_valid_resource()
        def goodId = response.json.id
        response = restBuilder.put("${getResourcePath()}/$goodId") {
            json putData
        }

        // "The response is correct"
        assert response.status == OK.value()
        //response.json
        assert TestTools.mapContains(response.json, putData, excludes)
        //get it and make sure
        def rget = restBuilder.get("${getResourcePath()}/$goodId")
        assert TestTools.mapContains(rget.json, putData, excludes)
        // subsetEquals(putData, rget.json, excludes)
        return rget
    }

    def testGet() {
        // "The save action is executed with valid data"
        def response = post_a_valid_resource()

        // "When the show action is called to retrieve a resource"
        def id = response.json.id
        response = restBuilder.get("${getResourcePath()}/$id")

        // "The response is correct"
        assert response.status == OK.value()
        assert response.json.id == id
        return response
    }

    def testDelete() {
        // "The save action is executed with valid data"
        def response = post_a_valid_resource()
        def id = response.json.id

        // "When the delete action is executed on an unknown instance"
        response = restBuilder.delete("${getResourcePath()}/99999")

        // "The response is bad"
        response.status == NOT_FOUND.value()

        // when: "When the delete action is executed on an existing instance"
        response = restBuilder.delete("$resourcePath/$id")

        // then: "The response is correct"
        assert response.status == NO_CONTENT.value()
        return response
    }

    def post_a_valid_resource() {
        def response = restBuilder.post(getResourcePath()) {
            json getPostData()
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
        true
    }

}
