/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus

import grails.rest.render.RenderContext
import yakworks.problem.IProblem

/**
 * Default renderer for JSON
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ProblemRenderer implements JsonRendererTrait<IProblem> {

    @Override
    @CompileDynamic
    void render(IProblem problem, RenderContext context) {
        setContentType(context)
        context.status = HttpStatus.valueOf(problem.status.code)
        // context.writer.write(jsonGenerator.toJson(problem))

        jsonBuilder(context).call {
            ok problem.ok
            status problem.status.code
            code problem.code
            title getMessage(problem)
            detail problem.detail
            errors problem.violations
        }
    }

    // swallow no such message exception and returns empty string
    String getMessage(IProblem problem){
        String message
        if(problem.msg) message = getMessage(problem.msg)

        if(!message && problem.title) {
            message = problem.title
        }
        return message
    }

}
