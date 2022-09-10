/*
* Copyright 2008 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.http

import groovy.transform.CompileStatic

import org.grails.plugins.testing.GrailsMockHttpServletResponse

import yakworks.json.groovy.JsonEngineTrait

@CompileStatic
class MockRestResponse extends GrailsMockHttpServletResponse implements JsonEngineTrait {

    /**
     * converts json body to map
     */
    Map bodyToMap() {
        return (Map)(contentAsString ? jsonSlurper.parseText(contentAsString) : [:])
    }

    /**
     * converts json body to map
     */
    List bodyToList() {
        return (List)(contentAsString ? jsonSlurper.parseText(contentAsString) : [])
    }
}
