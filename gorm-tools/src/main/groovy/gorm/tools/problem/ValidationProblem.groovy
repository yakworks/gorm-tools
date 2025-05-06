/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.problem

import groovy.transform.CompileStatic

import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import grails.util.GrailsUtil
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.problem.ProblemUtils
import yakworks.api.problem.ThrowableProblem
import yakworks.api.problem.Violation
import yakworks.api.problem.ViolationFieldError
import yakworks.api.problem.data.DataProblemException
import yakworks.api.problem.data.DataProblemTrait

/**
 * an extension of the default ValidationException so you can pass the entity and the message source
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class ValidationProblem implements DataProblemTrait<ValidationProblem> {

    public static String DEFAULT_CODE ='validation.problem'
    public static String DEFAULT_TITLE ='Validation Error(s)'

    /** The errors to convert to violations */
    Errors errors

    //overrides
    String defaultCode = DEFAULT_CODE
    String title = DEFAULT_TITLE
    ApiStatus status = HttpStatus.UNPROCESSABLE_ENTITY

    ValidationProblem() {}

    ValidationProblem(String message) {
        detail(message)
    }

    ValidationProblem errors(Errors v) {this.errors = v; return this;}

    ValidationProblem name(String nm){
        args.putIfAbsent('name', nm)
        return this
    }

    @Override
    ThrowableProblem toException(){
        //if it has a cause then use it, otherwise just throw the problem, the code here is verbose on purpose to make it easier to debug
        Throwable ex = getCause()
        if(ex){
            ex = GrailsUtil.deepSanitize(ex)
            return new ValidationProblem.Exception(getCause()).problem(this)
        } else {
            return new ValidationProblem.Exception().problem(this)
        }
    }

    static ValidationProblem of(Object entity, Throwable cause) {
        return ValidationProblem.of(cause).entity(entity);
    }

    /**
     * helper to create using entity for payload.
     */
    static ValidationProblem ofEntity(Object entity) {
        return new ValidationProblem().entity(entity);
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    static List<Violation> transateErrorsToViolations(Errors errs) {
        List<ViolationFieldError> errors = []
        if(!errs?.allErrors) return errors as List<Violation>

        for (ObjectError err : errs.allErrors) {
            String message = ProblemHandler.getMsg(err)
            ViolationFieldError fieldError = ViolationFieldError.of(err.code, message)
            if (err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors as List<Violation>
    }


    static class Exception extends DataProblemException {

        Exception(){ }
        Exception(Throwable cause){ super(cause)}

        @Override //throwable
        String getMessage() {
            def msg = ProblemUtils.buildMessage(problem)
            return getErrors() ? formatErrors(getErrors(), msg) : msg
        }

        ValidationProblem getValidationProblem() { return (ValidationProblem) problem }

        //helpers
        Errors getErrors() { getValidationProblem().errors}

        @Override
        String toString() {
            // String msg = ProblemUtils.problemToString(problem)
            // msg = "$msg - ${getMessage()}"
            return getMessage()
        }

        // @Override
        // ProblemException problem(ValidationProblem prob){
        //     this.problem = prob
        //     return this
        // }

        //Override it for performance improvement, because filling in the stack trace is quit expensive
        //FIXME make this configurable
        // @Override
        // synchronized Throwable fillInStackTrace() { return this }

        //Legacy from ValidationException
        static String formatErrors(Errors errors, String msg) {
            String ls = System.getProperty("line.separator");
            StringBuilder b = new StringBuilder();
            if (msg != null) {
                b.append(msg).append(" : ").append(ls);
            }

            for (ObjectError error : errors.getAllErrors()) {
                b.append(ls)
                    .append(" - ")
                    .append(error)
                    .append(ls);
            }
            return b.toString();
        }

    }
}
