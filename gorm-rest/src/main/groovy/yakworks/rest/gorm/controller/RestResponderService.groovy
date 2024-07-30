/*
* Copyright 2014 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.grails.plugins.web.rest.render.ServletRenderContext
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestContextHolder

import grails.rest.render.Renderer
import grails.rest.render.RendererRegistry
import grails.web.mime.MimeType

/**
 * Copy of Grails RestResponder but without the magic stuff that keeps having errors picked up
 *
 */
@CompileStatic
class RestResponderService {

    @Autowired RendererRegistry rendererRegistry

    void respond(Object value, Map args, Object responseFormats) {
        //this should never happen unless config is messed somehow
        if (rendererRegistry == null) throw new IllegalArgumentException("Houston we have a problem, no rendererRegistry")

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

        final GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        List<String> formats = calculateFormats(webRequest.actionName, args, responseFormats)

        final HttpServletResponse response = webRequest.getCurrentResponse()
        MimeType[] mimeTypes = getResponseFormat(response)

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
                if (rendererRegistry.isContainerType(valueType)) {
                    renderer = rendererRegistry.findContainerRenderer(mimeType, valueType, value)
                    if (renderer == null) {
                        renderer = rendererRegistry.findRenderer(mimeType, value)
                    }
                } else {
                    renderer = rendererRegistry.findRenderer(mimeType, value)
                }
            }

            if(renderer) break
        }

        if (!renderer) {
            //fall back to a json one
            renderer = rendererRegistry.findRenderer(MimeType.JSON, value)
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

    /**
     * gets the mime formats from either the args.formats or the responseFormatsProperty
     * @return the format list from args or responseFormatsProperty, or getDefaultResponseFormats() if those are null
     */
    private List<String> calculateFormats(String actionName, Map args, Object responseFormatsProperty) {
        if (args.formats) {
            return (List<String>) args.formats
        }

        if (responseFormatsProperty) {
            if (responseFormatsProperty instanceof List) {
                return (List<String>) responseFormatsProperty
            }
            if ((responseFormatsProperty instanceof Map) && actionName) {
                Map<String, Object> responseFormatsMap = (Map<String, Object>) responseFormatsProperty

                final responseFormatsForAction = responseFormatsMap.get(actionName)
                if (responseFormatsForAction instanceof List) {
                    return (List<String>) responseFormatsForAction
                }
            }
        }
        return getDefaultResponseFormats()
    }

    private MimeType[] getResponseFormat(HttpServletResponse response) {
        response.mimeTypesFormatAware
    }

    private List<String> getDefaultResponseFormats() {
        //TODO add csv
        return MimeType.getConfiguredMimeTypes()*.extension
    }

}
