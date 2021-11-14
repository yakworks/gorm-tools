/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus

import yakworks.api.problem.ProblemBase
import grails.rest.render.RenderContext

/**
 * Default renderer for JSON
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ProblemRenderer implements JsonRendererTrait<ProblemBase> {

    @Override
    void render(ProblemBase problem, RenderContext context) {
        setContentType(context)
        context.status = HttpStatus.valueOf(problem.status)
        context.writer.write(jsonGenerator.toJson(problem))
    }

}
