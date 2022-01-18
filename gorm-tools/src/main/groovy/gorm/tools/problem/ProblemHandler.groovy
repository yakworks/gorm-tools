/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.problem

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataAccessException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.support.MsgSourceResolvable
import grails.gorm.validation.ConstrainedProperty
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.i18n.icu.ICUMessageSource
import yakworks.problem.Problem
import yakworks.problem.ProblemException
import yakworks.problem.ProblemTrait
import yakworks.problem.UnexpectedProblem
import yakworks.problem.Violation
import yakworks.problem.ViolationFieldError
import yakworks.problem.data.DataProblem
import yakworks.problem.data.DataProblemCodes

/**
 * Service to prepare ApiError / ApiValidationError for given a given exception
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Slf4j
@CompileStatic
class ProblemHandler {

    @Autowired ICUMessageSource messageSource

    ProblemTrait<?> handleException(Class entityClass, Throwable e) {
        handleException(e, entityClass.simpleName)
    }

    /**
     * Prepares Problem for given entity and exception
     * - Problem(status:422) for ValidationException
     * - Problem(status:400) for DataAccessException
     * - Problem(status:404) for NotFoundException
     * - Problem(status:500) for other exceptions
     *
     * @param simpleName used for validation conversion
     * @param Exception e
     * @return ApiError
     */
    ProblemTrait<?> handleException(Throwable e, String simpleName = null) {
        // default error status code is 422
        ApiStatus status400 = HttpStatus.BAD_REQUEST
        ApiStatus status404 = HttpStatus.NOT_FOUND
        ApiStatus status422 = HttpStatus.UNPROCESSABLE_ENTITY

        if (e instanceof ValidationProblem.Exception) {
            def valProblem = e.getValidationProblem()
            if (valProblem.errors instanceof EmptyErrors) {
                //this is some other exception wrapped in validation exception
                valProblem.detail(e.cause?.message)
            }
            valProblem.violations(transateErrorsToViolations(valProblem.errors))
            return valProblem
        }
        else if (e instanceof ProblemTrait) {
            return (ProblemTrait) e
        }
        else if (e instanceof ProblemException) {
            return (ProblemTrait) e.problem
        }
        else if (e instanceof grails.validation.ValidationException
            || e instanceof org.grails.datastore.mapping.validation.ValidationException) {
            return buildFromErrorException(e, simpleName)
        } else if (e instanceof MsgSourceResolvable) {
            //legacy
            return Problem.ofCode(e.code).status(status400).detail(getMsg(e))
        } else if (e instanceof IllegalArgumentException) {
            //We use this all over to double as a validation error, Validate.notNull for example.
            return Problem.ofCode('error.illegalArgument').status(status400).detail(e.message)
        } else if (e instanceof DataAccessException) {
            //Not all will get translated in the repo as some get thrown after flush
            log.error("UNEXPECTED Data Access Exception ${e.message}", e)
            // Root of the hierarchy of data access exceptions
            if (isUniqueIndexViolation((DataAccessException) e)) {
                return DataProblemCodes.UniqueConstraint.ofCause(e)
            } else {
                return DataProblem.ofCause(e)
            }
        } else {
            return handleUnexpected(e)
        }
    }

    ProblemTrait<?> handleUnexpected(Throwable e){
        log.error("UNEXPECTED Internal Server Error ${e.message}", e)
        return UnexpectedProblem.ofCause(e).detail(e.message)
    }

    ValidationProblem buildFromErrorException(Throwable valEx, String entityName = null) {
        Errors ers = valEx['errors'] as Errors
        def valProb = ValidationProblem.ofCause(valEx).errors(ers)
        if(entityName) valProb.name(entityName)
        return valProb.violations(transateErrorsToViolations(ers))
    }

    String getMsg(MessageSourceResolvable msr) {
        //FIXME this should be generalized somehwere?
        try {
            return messageSource.getMessage(msr)
        }
        catch (e) {
            return msr.codes[0]
        }
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    //FIXME #339 see errormessageService, do we need some of that logic?
    List<Violation> transateErrorsToViolations(Errors errs) {
        List<ViolationFieldError> errors = []
        if(!errs?.allErrors) return errors as List<Violation>

        for (ObjectError err : errs.allErrors) {
            ViolationFieldError fieldError = ViolationFieldError.of(err.code, getMsg(err))
            if (err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors as List<Violation>
    }

    //Unique index unique constraint or primary key violation
    @SuppressWarnings('BracesForIfElse')
    static String isUniqueIndexViolation(DataAccessException dax) {
        String rootMessage = dax.rootCause.message
        if (rootMessage.contains("Unique index or primary key violation") || //mysql and H2
            rootMessage.contains("Duplicate entry") || //mysql
            rootMessage.contains("Violation of UNIQUE KEY constraint") || //sql server
            rootMessage.contains("unique constraint")) {
            return rootMessage
        } else {
            return null
        }
    }

    //Legacy from ValidationException
    static String formatErrors(Errors errors, String msg) {
        String ls = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        if (msg != null) {
            b.append(msg).append(" : ") //.append(ls);
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
