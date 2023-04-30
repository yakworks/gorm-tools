/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller


import groovy.transform.CompileStatic
import groovy.transform.Generated

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.problem.ProblemHandler
import grails.artefact.controller.RestResponder
import grails.artefact.controller.support.ResponseRenderer
import grails.util.GrailsNameUtils
import grails.web.api.ServletAttributes
import grails.web.servlet.mvc.GrailsParameterMap
import yakworks.api.problem.Problem
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.NameUtils
import yakworks.gorm.api.ApiUtils
import yakworks.gorm.config.GormConfig

/**
 * Marker trait with common helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 */
@CompileStatic
trait RestApiController implements RequestJsonSupport, RestResponder, RestRegistryResponder, ServletAttributes {

    @Autowired ProblemHandler problemHandler

    @Autowired GormConfig gormConfig

    //default responseFormats should be just json
    static List getResponseFormats() {
        return ['json', 'csv', 'xlsx']
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

    void handleException(Exception e) {
        Problem apiError = problemHandler.handleException(e)
        respondWith(apiError)
    }

    /**
     * Sometimes the stock getParams will loose the query params that are passed in. Its not clear why.
     * It will still contain the items from urlMapping such as id and the controller and action,
     * which is done in AbstractUrlMappingInfo.populateParamsForMapping.
     * This creates a new instance from the request which will then copy in the standard getParams().
     * 9999 out of 10000 when its not confused it results in the exact same Map, but when the params is missing the user query params it
     * will end up with the missing ones.
     * @return a new copy of the grails params
     */
    GrailsParameterMap getGrailsParams() {
        //the dispatchParams are the normal stock params, has values added in from UrlMappings and path
        Map dispatchParams = getParams()
        //if this hack is enabled
        if(gormConfig.enableGrailsParams) {
            def req = getRequest()
            //possibly from an async operation, still investigating, the params in the request get lost or dropped, but queryString still there
            if(!req.getParameterMap() && req.queryString) {
                Map parsedParams = ApiUtils.parseQueryParams(req.queryString)
                Map gParams = new GrailsParameterMap(parsedParams, req)
                // if the main params "dropped" then they will now be in gParams.
                // and the normal getParams will have what the UrlMappings added in and we put those into the newly parsed params
                gParams.putAll(dispatchParams)
                return gParams
            } else {
                //nothing should be wrong, params didnt get lost so just return the default
                return dispatchParams
            }
        } else {
            return dispatchParams
        }
    }

    // void respondWith(Object value, Map args = [:]) {
    //     internalRegistryRender value, args
    // }

    //*** RestResponder Overrides to use the RestRegistryResponder.respondWith
    @Generated @Override
    def respond(Object obj) { respondWith obj, [:] }

    @Generated @Override
    def respond(Object obj, Map args){ respondWith(obj, args) }

    @Generated @Override
    def respond(Map args, Object value) { respondWith value, args }

    @Generated @Override
    def respond(Map namedArgs, Map value) { respondWith value, namedArgs }

    @Generated @Override
    def respond(Map value) { respondWith value }

    /**
     * looks in params for value and converts to boolean, returning the defaultValue if not found
     */
    // boolean paramBoolean(String key, boolean defaultVal){
    //     return params.boolean(key, defaultVal)
    // }
}
