/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.renderer

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.rest.render.JsonRendererTrait
import grails.rest.render.RenderContext
import grails.web.mime.MimeType
import yakworks.api.ApiResults
import yakworks.api.ResultUtils

/**
 * Renderer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ApiResultsRenderer implements JsonRendererTrait<ApiResults> {

    @Override
    @CompileDynamic
    void render(ApiResults results, RenderContext context) {
        setContentType(context)
        jsonBuilder(context).call {
            ok results.ok
            status results.status.code
            code results.getCode()
            detail results.problems.detail
            problems results.problems
            title ResultUtils.getMessage(msgService, results)
            payload results.payload
        }
    }


    @Override
    MimeType[] getMimeTypes(){
        [MimeType.JSON, MimeType.TEXT_JSON] as MimeType[]
    }

}
