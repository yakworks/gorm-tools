/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.testing

import groovy.transform.CompileDynamic

import geb.spock.GebSpec

@SuppressWarnings(['NoDef', 'AbstractClassWithoutAbstractMethod', 'Indentation', 'JUnitTestMethodWithoutAssert'])
@CompileDynamic
abstract class RestApiFuncSpec extends GebSpec implements RestApiTestTrait {
    boolean vndHeaderOnError = false

    // String getResourcePath() { "${baseUrl}/${path}" }

    Map getInvalidData() { return [:] }

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
}
