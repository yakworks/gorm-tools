/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.etl.render

import groovy.transform.CompileStatic

import org.grails.plugins.web.rest.render.ServletRenderContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver

import grails.core.GrailsApplication
import grails.core.support.proxy.DefaultProxyHandler
import grails.core.support.proxy.ProxyHandler
import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import yakworks.etl.excel.ExcelBuilder
import yakworks.i18n.icu.ICUMessageSource
import yakworks.message.MsgKey

/**
 * CSV writer
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait XlsRendererTrait<T> implements Renderer<T> {

    public static final MimeType XLSX_TYPE = new MimeType('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', "xlsx")

    MimeType[] mimeTypes = [XLSX_TYPE] as MimeType[]
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
        if (!targetType) this.targetType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), XlsRendererTrait)
        return targetType
    }

    ExcelBuilder excelBuilder(RenderContext context) {
        def servletContext = (ServletRenderContext) context
        return ExcelBuilder.of(servletContext.webRequest.response.outputStream).build()
    }

    void setContentType(RenderContext context){
        final mimeType = context.acceptMimeType ?: XLSX_TYPE
        context.setContentType( GrailsWebUtil.getContentType(mimeType.name, encoding) )
    }

    String getMessage(MsgKey msgKey){
        msgService.get(msgKey)
    }
}
