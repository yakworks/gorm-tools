/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.transform.CompileStatic

import gorm.tools.api.problem.Problem
import grails.rest.render.RenderContext
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType

/**
 * Default renderer for JSON
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ProblemRenderer extends JsonGeneratorRenderer<Problem>{

    ProblemRenderer() {
        super(Problem)
    }

    @Override
    void render(Problem problem, RenderContext context) {
        setContentType(context)
        context.writer.write(jsonGenerator.toJson(problem))
    }

}
