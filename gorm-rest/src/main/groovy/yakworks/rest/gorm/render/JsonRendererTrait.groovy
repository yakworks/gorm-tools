/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic

import grails.rest.render.RenderContext
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import yakworks.json.groovy.JsonEngineTrait

/**
 * Json Renderer that uses the default groovy2.5 jsonGenerator
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait JsonRendererTrait<T> implements RendererTrait<T>, JsonEngineTrait {

    @Override
    MimeType[] getMimeTypes(){
        [MimeType.JSON, MimeType.TEXT_JSON] as MimeType[]
    }

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

}
