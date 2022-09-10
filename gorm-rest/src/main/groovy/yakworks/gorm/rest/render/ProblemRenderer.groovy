/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.rest.render

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus

import grails.rest.render.RenderContext
import yakworks.api.problem.Problem

/**
 * Default Problem renderer.
 * Overriden to se the 'application/problem+json' content type and the status from the Problem.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ProblemRenderer implements JsonRendererTrait<Problem> {

    @Override
    @CompileDynamic
    void render(Problem problem, RenderContext context) {
        setContentType(context)
        context.status = HttpStatus.valueOf(problem.status.code)
        // context.writer.write(jsonGenerator.toJson(problem))
        def data = problem.asMap()
        jsonBuilder(context).call(data)
    }

    void setContentType(RenderContext context){
        context.setContentType( 'application/problem+json' )
    }

}
