/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.testing

import groovy.transform.CompileDynamic

import grails.plugins.rest.client.RestBuilder

//@CompileStatic
@CompileDynamic
trait RestApiTestTrait {

    //private static GrailsApplication _grailsApplication
    //private static Object _servletContext

    RestBuilder getRestBuilder() {
        new RestBuilder()
    }

    List<String> getExcludes() { [] }

    // String getResourcePath() {
    //     "${baseUrl}/api/project"
    // }

    /**
     * Loosely test 2 maps for equality
     * asserts more or less that subset:[a: 1, b: 2] == full:[a: 1, b: 2, c: 3]
     * http://csierra.github.io/posts/2013/02/12/loosely-test-for-map-equality-using-groovy/
     *
     * @param subset the partial map
     * @param full the full map
     * @param excludes  the list of fields to exclude from the subset
     */
    boolean subsetEquals(Map subset, Map full, List<String> excludes = []) {
        //if (!full.keySet().containsAll(subset.keySet())) return false
        subset.findAll { !excludes.contains(it.key) }
            .every { it.value == full[it.key] }
    }

}
