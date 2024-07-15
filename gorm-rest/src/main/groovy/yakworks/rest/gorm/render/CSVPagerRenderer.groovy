/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.plugins.web.rest.render.ServletRenderContext

import gorm.tools.beans.Pager
import grails.rest.render.RenderContext
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import yakworks.etl.csv.CSVMapWriter

/**
 * Rederer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class CSVPagerRenderer implements RendererTrait<Pager> {

    public static final MimeType TEXT_CSV = new MimeType('text/csv', "csv")

    MimeType[] mimeTypes = [TEXT_CSV] as MimeType[]

    @Override
    //@CompileDynamic
    void render(Pager pager, RenderContext context) {
        setContentType(context)
        setContentDisposition(context)
        csvWriter(context).writeCsv(pager.data)
    }

    //TODO should we set the file name?
    void setContentDisposition(RenderContext context){
        def servletCtx = (ServletRenderContext)context
        def name = context.getControllerName()
        servletCtx.webRequest.response.setHeader("Content-Disposition", "attachment;filename=\"${name}.csv\"")
    }

    CSVMapWriter csvWriter(RenderContext context) {
        return CSVMapWriter.of(context.writer)
    }

    void setContentType(RenderContext context){
        final mimeType = context.acceptMimeType ?: TEXT_CSV
        context.setContentType( GrailsWebUtil.getContentType(mimeType.name, encoding) )
    }

}
