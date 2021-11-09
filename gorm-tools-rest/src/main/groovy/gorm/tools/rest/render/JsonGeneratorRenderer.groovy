/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import grails.core.GrailsApplication
import grails.core.support.proxy.DefaultProxyHandler
import grails.core.support.proxy.ProxyHandler
import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import yakworks.commons.json.JsonEngineTrait

/**
 * Json Renderer that uses the default groovy2.5 jsonGenerator
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class JsonGeneratorRenderer<T> implements Renderer<T>, JsonEngineTrait {

    final Class<T> targetType
    MimeType[] mimeTypes = [MimeType.JSON, MimeType.TEXT_JSON] as MimeType[]
    String encoding = 'UTF-8'

    @Autowired
    GrailsApplication grailsApplication

    @Autowired(required = false)
    ProxyHandler proxyHandler = new DefaultProxyHandler()

    /**
     * The properties to be included
     */
    List<String> includes
    /**
     * The properties to be excluded
     */
    List<String> excludes = []

    JsonGeneratorRenderer(Class<T> targetType) {
        this.targetType = targetType
    }

    @Override
    void render(T entity, RenderContext context) {
        setContentType(context)
        context.writer.write(jsonGenerator.toJson(entity))
    }

    void setContentType(RenderContext context){
        final mimeType = context.acceptMimeType ?: MimeType.JSON
        context.setContentType( GrailsWebUtil.getContentType(mimeType.name, encoding) )
    }
}
