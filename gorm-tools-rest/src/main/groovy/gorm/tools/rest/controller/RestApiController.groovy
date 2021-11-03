/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import javax.servlet.http.HttpServletRequest

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import gorm.tools.json.JsonParserTrait
import grails.artefact.controller.RestResponder
import grails.artefact.controller.support.ResponseRenderer
import grails.util.GrailsNameUtils
import grails.web.api.ServletAttributes
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.NameUtils

/**
 * Marker trait with common helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 */
@CompileStatic
trait RestApiController implements JsonParserTrait, RestResponder, ServletAttributes {

    //default responseFormats should be just json
    static List getResponseFormats() {
        return ['json']
    }

    JsonSlurper getJsonSlurper(){
        return JsonEngine.slurper
    }

    /**
     * Parse a JSON Map data structure from request body and casts to Map<String, Object>.
     * returns an empty Map if its any other object, use parseJsonList if a list is expected
     *
     * @param req the request with the json body
     * @return a Map data structure
     */
    Map<String, Object> parseJson() {
        final jsonObj = parseJsonObject(getRequest())
        return ( jsonObj instanceof Map ? jsonObj : Collections.emptyMap() ) as Map<String, Object>
    }

    /**
     * Parse a JSON data structure from request body
     *
     * @param req the request with the json body
     * @return a data structure of lists and maps
     */
    Object parseJsonObject(HttpServletRequest req) {
        //|| !ignoredRequestBodyMethods.contains(method)) {
        if (req.contentLength == 0) {
            return Collections.emptyMap()
        }
        return getJsonSlurper().parse(req.inputStream)
    }

    /**
     * getControllerName() works inisde a request and should be used, but during init or outside a request use this
     * should give roughly what logicalName is which is used to setup the urlMappings by default
     */
    String getLogicalControllerName(){
        String logicalName = GrailsNameUtils.getLogicalName(this.class, 'Controller')
        return NameUtils.getPropertyName(logicalName)
    }

    /**
     * Deprecated, just calls parseJson(getRequest())
     * TODO maybe keep this one and have it be the one that merges the json body with params
     */
    Map getDataMap() {
        return parseJson(getRequest())
    }

    /**
     * Cast this to ResponseRenderer and call render
     * this allows us to call the render, keeping compile static without implementing the Trait
     * as the trait get implemented with AST magic by grails.
     * This is how the RestResponder does it but its private there
     */
    void callRender(Map args) {
        ((ResponseRenderer) this).render args
    }

    void callRender(Map argMap, CharSequence body){
        ((ResponseRenderer) this).render(argMap, body)
    }

    //public instance getter for static namespace
    String getNamespaceProperty(){
        ClassUtils.getStaticPropertyValue(this.class.metaClass, 'namespace') as String
    }

}
