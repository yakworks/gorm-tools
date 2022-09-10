/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.rest.render

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus

import grails.rest.render.RenderContext
import yakworks.api.ApiResults

/**
 * Concrete so we can set the HttpStatus.MULTI_STATUS
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
        context.status = HttpStatus.MULTI_STATUS
        jsonBuilder(context).call(results.asMap())
    }

}
