/*
* Copyright 2014 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic
import groovy.transform.Generated

import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.plugins.web.rest.render.ServletRenderContext
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

import gorm.tools.utils.BenchmarkHelper
import grails.artefact.Controller
import grails.core.support.proxy.ProxyHandler
import grails.rest.render.Renderer
import grails.rest.render.RendererRegistry
import grails.web.mime.MimeType

/**
 * Grails RestResponder but without the magic stuff that keeps having errors picked up
 *
 */
@CompileStatic
trait RestRegistryResponder {

    private String PROPERTY_RESPONSE_FORMATS = "responseFormats"

    private RendererRegistry rendererRegistry
    private ProxyHandler proxyHandler

    @Generated
    @Autowired(required = false)
    void setRendererRegistry(RendererRegistry rendererRegistry) {
        this.rendererRegistry = rendererRegistry
    }

    @Generated
    RendererRegistry getRendererRegistry() {
        return this.rendererRegistry
    }

    @Generated
    @Autowired(required = false)
    void setProxyHandler(ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler
    }

    @Generated
    ProxyHandler getProxyHandler() {
        return this.proxyHandler
    }

    /**
     * Call the internalRegistryRender
     * Changes so it does do anything for Errors objects
     */
    @Generated
    void respondWith(Object value, Map args = [:]) {
        internalRegistryRender value, args
    }

    void internalRegistryRender(Object value, Map args=[:]) {
        // BenchmarkHelper.startTime()
        Integer statusCode
        if (args.status) {
            final statusValue = args.status
            if (statusValue instanceof Number) {
                statusCode = statusValue.intValue()
            } else {
                if (statusValue instanceof HttpStatus) {
                    statusCode = ((HttpStatus)statusValue).value()
                } else {
                    statusCode = statusValue.toString().toInteger()
                }
            }
        }

        final GrailsWebRequest webRequest = ((Controller)this).getWebRequest()
        List<String> formats = calculateFormats(webRequest.actionName, args)

        final HttpServletResponse response = webRequest.getCurrentResponse()
        MimeType[] mimeTypes = getResponseFormat(response)

        RendererRegistry registry = rendererRegistry
        if (registry == null) throw new IllegalArgumentException("Houston we have a problem")

        Renderer renderer = null

        for(MimeType mimeType in mimeTypes) {
            if (mimeType == MimeType.ALL && formats) {
                final allMimeTypes = MimeType.getConfiguredMimeTypes()
                final firstFormat = formats[0]
                mimeType = allMimeTypes.find { MimeType mt -> mt.extension == firstFormat}
                if(mimeType) {
                    webRequest.currentRequest.setAttribute(GrailsApplicationAttributes.RESPONSE_MIME_TYPE, mimeType)
                }
            }

            if (mimeType && formats.contains(mimeType.extension)) {
                final Class<?> valueType = value.getClass()
                if (registry.isContainerType(valueType)) {
                    renderer = registry.findContainerRenderer(mimeType, valueType, value)
                    if (renderer == null) {
                        renderer = registry.findRenderer(mimeType, value)
                    }
                } else {
                    renderer = registry.findRenderer(mimeType, value)
                }
            }

            if(renderer) break
        }

        if (!renderer) {
            //fall back to a json one
            renderer = registry.findRenderer(MimeType.JSON, value)
            if(!renderer)
                throw new IllegalArgumentException("Houston we have a problem, renderer can't be found for fallback json format and ${value.class}")
        }


        final ServletRenderContext context = new ServletRenderContext(webRequest, args)
        if(statusCode != null) context.status = HttpStatus.valueOf(statusCode)

        renderer.render(value, context)

        // BenchmarkHelper.printEndTimeMsg("${renderer.class} render")

        if(context.wasWrittenTo() && !response.isCommitted()) {
            response.flushBuffer()
        }

    }

    private List<String> calculateFormats(String actionName, Map args) {
        if (args.formats) {
            return (List<String>) args.formats
        }

        if (this.hasProperty(PROPERTY_RESPONSE_FORMATS)) {
            final responseFormatsProperty = InvokerHelper.getProperty(this, PROPERTY_RESPONSE_FORMATS)
            if (responseFormatsProperty instanceof List) {
                return (List<String>) responseFormatsProperty
            }
            if ((responseFormatsProperty instanceof Map) && actionName) {
                Map<String, Object> responseFormatsMap = (Map<String, Object>) responseFormatsProperty

                final responseFormatsForAction = responseFormatsMap.get(actionName)
                if (responseFormatsForAction instanceof List) {
                    return (List<String>) responseFormatsForAction
                }
                return getDefaultResponseFormats()
            }
            return getDefaultResponseFormats()
        }
        return getDefaultResponseFormats()
    }

    // @CompileDynamic
    private MimeType[] getResponseFormat(HttpServletResponse response) {
        response.mimeTypesFormatAware
    }

    private List<String> getDefaultResponseFormats() {
        //add csv
        return MimeType.getConfiguredMimeTypes()*.extension
    }

}
