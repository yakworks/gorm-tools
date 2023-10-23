/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.json

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import gorm.tools.problem.ValidationProblem
import yakworks.api.problem.Problem
import yakworks.api.problem.Violation
import yakworks.api.problem.ViolationFieldError

/**
 * Proof of concept to stop stack overflow on ApiResults renderer.
 * FIXME move to gorm-tools. get tests in place both in rcm and in gorm-tools
 */
@CompileStatic
class ProblemConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        Problem.isAssignableFrom(type)
    }

    @Override
    Object convert(Object value, String key) {
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
        if(!violations && p instanceof ValidationProblem && p.getErrors()){
            violations = transformErrorsToViolations((p as ValidationProblem).getErrors())
        }
        if(violations) props.errors = violations
        return props
    }

    List<Violation> transformErrorsToViolations(Errors errs) {
        List<ViolationFieldError> errors = []
        if(!errs?.allErrors) return errors as List<Violation>

        for (ObjectError err : errs.allErrors) {
            //TODO get getMsg hooked up, using defaultMessage for message for now, should be ViolationFieldError.of(err.code, getMsg(err))
            ViolationFieldError fieldError = ViolationFieldError.of(err.code, err.defaultMessage)
            if (err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors as List<Violation>
    }
}
