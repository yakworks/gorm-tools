/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller


import groovy.transform.CompileStatic
import groovy.transform.Generated

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.problem.ProblemHandler
import grails.artefact.controller.RestResponder
import grails.artefact.controller.support.ResponseRenderer
import grails.util.GrailsNameUtils
import grails.web.api.ServletAttributes
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.NameUtils
import yakworks.problem.ProblemTrait

/**
 * Marker trait with common helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 */
@CompileStatic
trait RestApiController implements RequestJsonSupport, RestResponder, RestRegistryResponder, ServletAttributes {

    @Autowired
    ProblemHandler problemHandler

    //default responseFormats should be just json
    static List getResponseFormats() {
        return ['json']
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
        ProblemTrait apiError = problemHandler.handleException(e)
        respond(apiError)
    }

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
    boolean paramBoolean(String key, boolean defaultVal){
        return params[key] ? params[key] as Boolean : defaultVal
    }
}
