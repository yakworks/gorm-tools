/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv.render

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.plugins.web.rest.render.ServletRenderContext

import gorm.tools.beans.Pager
import grails.rest.render.RenderContext

/**
 * Rederer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class CSVPagerRenderer implements CSVRendererTrait<Pager> {

    @Override
    @CompileDynamic
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

}
