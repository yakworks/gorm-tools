/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.api.ApiResults
import grails.rest.render.RenderContext

/**
 * Renderer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ApiResultsRenderer implements JsonRendererTrait<ApiResults>{

    @Override
    @CompileDynamic
    void render(ApiResults results, RenderContext context) {
        setContentType(context)
        jsonBuilder(context).call {
            ok results.ok
            status results.status
            code results.getCode()
            title getMessage(results)
        }
    }

    // checks for title
    String getMessage(ApiResults results){
        String message = results.title
        if(!message){
            if(results.code){
                msgService.getMessageSafe(results.toMessageSource())
            } else if(results.size() != 0) {
                results.code = results[0].code
            }
        }
        return message
    }

}
