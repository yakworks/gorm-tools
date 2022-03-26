/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv.render


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
import yakworks.i18n.MsgKey
import yakworks.i18n.icu.ICUMessageSource

/**
 * CSV writer
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait CSVRendererTrait<T> implements Renderer<T> {

    public static final MimeType TEXT_CSV = new MimeType('text/csv', "csv")

    MimeType[] mimeTypes = [TEXT_CSV] as MimeType[]
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
        if (!targetType) this.targetType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), CSVRendererTrait)
        return targetType
    }

    CSVMapWriter csvWriter(RenderContext context) {
        return CSVMapWriter.of(context.writer)
    }

    void setContentType(RenderContext context){
        final mimeType = context.acceptMimeType ?: TEXT_CSV
        context.setContentType( GrailsWebUtil.getContentType(mimeType.name, encoding) )
    }

    String getMessage(MsgKey msgKey){
        msgService.get(msgKey)
    }
}
