/*
* Copyright 2008 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.http

import javax.servlet.ServletContext

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.plugins.testing.GrailsMockHttpServletRequest

import yakworks.json.groovy.JsonEngineTrait

/**
 * A custom mock HTTP servlet request that provides the extra properties
 * and methods normally injected by the "servlets" plugin.
 */
@CompileStatic
class MockRestRequest extends GrailsMockHttpServletRequest implements JsonEngineTrait {

    Object cachedJson

    MockRestRequest() {
        super();
        method = 'GET'
        setContentType('application/json; charset=UTF-8')
        setFormat('json')
    }

    MockRestRequest(ServletContext servletContext) {
        super(servletContext);
        method = 'GET'
        setContentType('application/json; charset=UTF-8')
        setFormat('json')
    }

    /**
     * overrides to use defualt groovy json parser
     */
    @Override
    @CompileDynamic
    void setJson(Object sourceJson) {
        if (sourceJson instanceof String) {
            setContent(sourceJson.getBytes("UTF-8"))
        }
        else {
            setContent(jsonGenerator.toJson(sourceJson).getBytes("UTF-8"))
        }
        getAttribute("org.codehaus.groovy.grails.WEB_REQUEST")?.informParameterCreationListeners()
    }

    /**
     * Uses the groovy slurper to parse the json
     */
    @Override
    def getJSON() {
        getJson()
    }


    def getJson() {
        if (!cachedJson) {
            if (this.contentLength == 0) {
                cachedJson = Collections.emptyMap()
            } else {
                cachedJson = jsonSlurper.parse(this.inputStream)
            }
        }
        return cachedJson
    }

}
