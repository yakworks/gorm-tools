/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.plugins.web.rest.render.ServletRenderContext
import org.springframework.http.HttpStatus

import gorm.tools.beans.Pager
import grails.rest.render.RenderContext
import yakworks.etl.excel.ExcelBuilder
import yakworks.etl.excel.ExcelBuilderSupport
import yakworks.meta.MetaMapList

/**
 * Rederer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
@Slf4j
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
        eb.createHeader(dataList)
            .writeData(dataList)
        try {
            eb.writeOut()
            context.setStatus(HttpStatus.OK)
        } catch(Exception ex) {
            //catch any exception thrown while writing xsl file to servlet response output stream
            //Because if exception is thrown at this place, (eg because socket was closed by client) nothing further action can be taken
            //and trying to send any more response in form of problem result etc would result in another exception
            //see #2596
            String entityClassName = params['entityClassName']
            if(dataList instanceof MetaMapList && dataList.metaEntity) {
                entityClassName = dataList.metaEntity.className
            }
            log.warn("Error encountered while rendering xsl file, domain:$entityClassName, error:${ex.message}")
        }
    }

    void setContentDisposition(RenderContext context){
        def servletCtx = (ServletRenderContext)context
        def name = context.getControllerName()
        servletCtx.webRequest.response.setHeader("Content-Disposition", "attachment;filename=\"${name}.xlsx\"")
    }

}
