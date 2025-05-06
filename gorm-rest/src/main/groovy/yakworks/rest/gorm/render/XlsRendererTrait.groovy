/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileStatic

import org.grails.plugins.web.rest.render.ServletRenderContext
import org.springframework.beans.factory.annotation.Autowired

import grails.rest.render.RenderContext
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import yakworks.etl.excel.ExcelBuilder
import yakworks.gorm.api.ApiConfig
import yakworks.gorm.api.IncludesConfig
import yakworks.message.MsgKey

/**
 * CSV writer
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait XlsRendererTrait<T> implements RendererTrait<T> {

    public static final MimeType XLSX_TYPE = new MimeType('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', "xlsx")

    MimeType[] mimeTypes = [XLSX_TYPE] as MimeType[]

    @Autowired
    IncludesConfig includesConfig

    @Autowired
    ApiConfig apiConfig

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
