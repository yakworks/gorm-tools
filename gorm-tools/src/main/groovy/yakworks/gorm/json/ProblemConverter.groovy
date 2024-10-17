/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.json

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

import gorm.tools.problem.ValidationProblem
import yakworks.api.problem.Problem

/**
 * Groovy json converter for Problem
 */
@CompileStatic
class ProblemConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        Problem.isAssignableFrom(type)
    }

    @Override
    Object convert(Object value, String key) {
        //FIXME why is this not using the asMap?
        def p = (Problem)value
        Map props = [
            ok: p.ok,
            type: p.type,
            status: p.status.code,
            code: p.code,
            title: p.title,
            // title: ResultUtils.getMessage(msgService, p),
            detail: p.detail
        ] as Map<String, Object>

        List violations = p.violations
        //if its a ValidationProblem and has unstranslated errors then convert them
        if(!violations && p instanceof ValidationProblem && p.getErrors().hasErrors()){
            violations = ValidationProblem.transateErrorsToViolations((p as ValidationProblem).getErrors())
        }
        if(violations) props.errors = violations
        return props
    }

}
