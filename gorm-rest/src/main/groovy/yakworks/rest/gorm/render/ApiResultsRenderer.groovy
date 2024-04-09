/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus

import grails.rest.render.RenderContext
import grails.web.mime.MimeType
import yakworks.api.ApiResults
import yakworks.api.ResultUtils

/**
 * Concrete so we can set the HttpStatus.MULTI_STATUS
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ApiResultsRenderer implements JsonRendererTrait<ApiResults>{

    @Override
    void render(ApiResults results, RenderContext context) {
        setContentType(context)
        //FIXME do we want this be automatically multi?
        //context.status = HttpStatus.MULTI_STATUS
        var dataMap = results.asMap()

        //FIXME do we need this?
        //hack for now, this will try and get message from first item in the ApiResults list
        if(!dataMap.title && results.list.size() != 0) {
            //use msg form first item
            dataMap.title = msgService.get(results.list[0].msg)
        }
        //FIXME do we need this?
        if(!dataMap.containsKey("problems")) dataMap.problems = []

        jsonBuilder(context).call(dataMap)
    }

    // @Override
    // void render(ApiResults results, RenderContext context) {
    //     setContentType(context)
    //     jsonBuilder(context).call(
    //         ok: results.ok,
    //         status: results.status.code,
    //         code: results.getCode(),
    //         title: ResultUtils.getMessage(msgService, results),
    //         problems: results.problems,
    //         payload: results.payload
    //     )
    // }

    //If not specified was getting Duplicate method name "getMimeTypes" with signature, even though it overriden in JsonRendererTrait
    // @Override
    // MimeType[] getMimeTypes(){
    //     [MimeType.JSON, MimeType.TEXT_JSON] as MimeType[]
    // }

}
