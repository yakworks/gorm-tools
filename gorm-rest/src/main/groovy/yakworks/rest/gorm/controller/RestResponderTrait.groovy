/*
* Copyright 2014 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import groovy.transform.CompileStatic
import groovy.transform.Generated

import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired

import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Copy of Grails RestResponder but without the magic stuff that keeps having errors picked up
 *
 */
@CompileStatic
trait RestResponderTrait {

    @Autowired RestResponderService restResponderService

    //will get implemented by normal controller and WebAttributes
    //FIXME change to what we end up with with our override for getParamsMap()
    abstract GrailsParameterMap getParams()

    /**
     * Call the internalRegistryRender
     * Changes so it does do anything for Errors objects
     */
    @Generated
    void respondWith(Object value) {
        respondWith(value, [:])
    }

    @Generated
    void respondWith(Object value, Map args) {
        Object responseFormatsProp = getConfiguredResponseFormats()
        //put params into arguments so we can access them from a Renderer, used for the excel renderer for example
        if(!args.params) args.params = getParams()
        internalRegistryRender(value, args, responseFormatsProp)
    }

    void internalRegistryRender(Object value, Map args, Object responseFormats) {
        restResponderService.respond(value, args, responseFormats)
    }

    /**
     * gets value of this controllers responseFormats
     */
    private Object getConfiguredResponseFormats(){
        Object responseFormatsProperty = null
        if (this.hasProperty("responseFormats")) {
            responseFormatsProperty = InvokerHelper.getProperty(this, "responseFormats")
        }
        return responseFormatsProperty
    }

}
