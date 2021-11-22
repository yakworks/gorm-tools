/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import grails.rest.render.RenderContext
import yakworks.api.ApiResults

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
            status results.status.code
            code results.getCode()
            title getMessage(results)
        }
    }

    // swallow no such message exception and returns empty string
    String getMessage(ApiResults results){
        String message
        if(results.msg) message = getMessage(results.msg)

        if(!message && results.title) {
            message = results.title
        } else if(results.size() != 0) {
            message = getMessage(results[0].msg)
        }
        return message
    }

}
