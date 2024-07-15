/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileStatic

import grails.rest.render.RenderContext
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import yakworks.etl.csv.CSVMapWriter

/**
 * Commons helpers for CSV Rendering
 *
 * @see CSVPagerRenderer
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait CSVRendererTrait<T> implements RendererTrait<T> {

    public static final MimeType TEXT_CSV = new MimeType('text/csv', "csv")

    MimeType[] mimeTypes = [TEXT_CSV] as MimeType[]

    CSVMapWriter csvWriter(RenderContext context) {
        return CSVMapWriter.of(context.writer)
    }

    void setContentType(RenderContext context){
        final mimeType = context.acceptMimeType ?: TEXT_CSV
        context.setContentType( GrailsWebUtil.getContentType(mimeType.name, encoding) )
    }

}
