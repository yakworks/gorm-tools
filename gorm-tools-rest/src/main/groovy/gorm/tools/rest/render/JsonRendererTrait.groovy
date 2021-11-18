/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver

import grails.core.GrailsApplication
import grails.core.support.proxy.DefaultProxyHandler
import grails.core.support.proxy.ProxyHandler
import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import yakworks.commons.json.JsonEngineTrait
import yakworks.i18n.MsgKey
import yakworks.i18n.icu.ICUMessageSource

/**
 * Json Renderer that uses the default groovy2.5 jsonGenerator
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait JsonRendererTrait<T> implements Renderer<T>, JsonEngineTrait {

    MimeType[] mimeTypes = [MimeType.JSON, MimeType.TEXT_JSON] as MimeType[]
    String encoding = 'UTF-8'

    @Autowired
    GrailsApplication grailsApplication

    @Autowired(required = false)
    ProxyHandler proxyHandler = new DefaultProxyHandler()

    @Autowired
    ICUMessageSource msgService

    Class<T> targetType

    @Override
    Class<T> getTargetType() {
        if (!targetType) this.targetType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), JsonRendererTrait)
        return targetType
    }

    /**
     * The properties to be included
     */
    // List<String> includes
    /**
     * The properties to be excluded
     */
    // List<String> excludes = []

    @Override
    void render(T entity, RenderContext context) {
        setContentType(context)
        context.writer.write(jsonGenerator.toJson(entity))
    }

    StreamingJsonBuilder jsonBuilder(RenderContext context) {
        return new StreamingJsonBuilder(context.writer, jsonGenerator)
    }

    void setContentType(RenderContext context){
        final mimeType = context.acceptMimeType ?: MimeType.JSON
        context.setContentType( GrailsWebUtil.getContentType(mimeType.name, encoding) )
    }

    // swallow no such message exception and returns empty string
    String getMessage(MsgKey msgKey){
        msgService.getMessage(msgKey)
    }
}
