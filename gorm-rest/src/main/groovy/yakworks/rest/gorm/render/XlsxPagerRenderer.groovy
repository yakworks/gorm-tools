/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileStatic

import org.grails.plugins.web.rest.render.ServletRenderContext
import org.springframework.http.HttpStatus

import gorm.tools.beans.Pager
import grails.rest.render.RenderContext
import yakworks.etl.excel.ExcelBuilder
import yakworks.etl.excel.ExcelBuilderSupport

/**
 * Rederer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class XlsxPagerRenderer implements XlsRendererTrait<Pager> {

    @Override
    void render(Pager pager, RenderContext context) {

        setContentType(context)
        setContentDisposition(context)
        ExcelBuilder eb = excelBuilder(context)

        def dataList = pager.data

        Map params = getParams(context)
        //for future use
        // String headers = params['headers']
        // String includesKey = params['includesKey']
        //if includes was passed then dont use whats in the grid config
        if(!params['includes']){
            String entityClassName = params['entityClassName']
            //look in config and match whats there if not specified
            ExcelBuilderSupport.useIncludesConfig(eb, apiConfig, dataList, entityClassName)
        }
        eb.writeData(dataList as List<Map>)
        eb.writeOut()
        context.setStatus(HttpStatus.OK)
    }

    void setContentDisposition(RenderContext context){
        def servletCtx = (ServletRenderContext)context
        def name = context.getControllerName()
        servletCtx.webRequest.response.setHeader("Content-Disposition", "attachment;filename=\"${name}.xlsx\"")
    }

}
