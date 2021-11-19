/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSourceResolvable
import org.springframework.dao.DataAccessException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.support.MsgSourceResolvable
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.problem.Problem
import yakworks.problem.ProblemTrait
import yakworks.problem.Violation
import yakworks.problem.ViolationFieldError
import yakworks.i18n.MsgKey
import yakworks.i18n.icu.ICUMessageSource

/**
 * Service to prepare ApiError / ApiValidationError for given exception
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Slf4j
@CompileStatic
class ProblemHandler {

    @Autowired ICUMessageSource messageSource

    ProblemTrait handleException(Throwable e) {
        handleException("Entity", e)
    }

    ProblemTrait handleException(Class entityClass, Throwable e) {
        handleException(entityClass.simpleName, e)
    }

    /**
     * Prepares ApiError for given entity and exception
     * - ApiError(status:422) for EntityValidationException, ValidationException and DataAccessException
     * - ApiError(status:404) for EntityNotFoundException
     * - ApiError(status:500) for other exceptions
     *
     * @param entityName domain class
     * @param Exception e
     * @return ApiError
     */
    ProblemTrait handleException(String entityName, Throwable e) {
        // default error status code is 422
        ApiStatus status400 = HttpStatus.BAD_REQUEST
        ApiStatus status404 = HttpStatus.NOT_FOUND
        ApiStatus status422 = HttpStatus.UNPROCESSABLE_ENTITY

        if (e instanceof EntityValidationProblem) {
            if(e.errors instanceof EmptyErrors){
                //this is some other exception wrapped in validation exception
                e.detail( e.cause?.message)
            }
            e.violations(transateErrorsToViolations(e.errors))
            return e
        }
        else if (e instanceof ProblemTrait) {
            // already a problem then just return it
            return (ProblemTrait) e
        }
        else if (e instanceof grails.validation.ValidationException) {
            return buildFromErrorException(entityName, e)
        }
        else if (e instanceof org.grails.datastore.mapping.validation.ValidationException) {
            return buildFromErrorException(entityName, e)
        }
        else if (e instanceof MsgSourceResolvable) { //legacy
            return Problem.of(status400).msg(MsgKey.of(e.code)).detail(getMsg(e))
        }
        else if (e instanceof IllegalArgumentException) {
            //We use this all over to double as a validation error, Validate.notNull for example.
            return Problem.of(status400).msg(MsgKey.of('error.illegalArgument')).detail(e.message)
        }
        else if (e instanceof DataAccessException) {
            //Not all will get translated in the repo as some get thrown after flush
            log.error("UNEXPECTED Data Access Exception ${e.message}", e)
            // Root of the hierarchy of data access exceptions
            if(isUniqueIndexViolation((DataAccessException)e)){
                return UniqueConstraintProblem.of(e)
            } else {
                return DataAccessProblem.of(e)
            }
        }
        else {
            log.error("UNEXPECTED Internal Server Error ${e.message}", e)
            return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR).msg(MsgKey.of('error.unhandled')).detail(e.message)
        }
    }

    EntityValidationProblem buildFromErrorException(String entityName, Throwable valEx){
        Errors ers = valEx['errors'] as Errors
        def valProb = EntityValidationProblem.of(valEx).name(entityName).errors(ers)
        return valProb.violations(transateErrorsToViolations(ers))
    }

    String getMsg(MessageSourceResolvable msr){
        String msg = messageSource.getMessage(msr)
        return msg
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    //FIXME #339 see errormessageService, do we need some of that logic?
    List<Violation> transateErrorsToViolations(Errors errs) {
        List<ViolationFieldError> errors = []
        for(ObjectError err : errs.allErrors) {
            ViolationFieldError fieldError = ViolationFieldError.of(err.code, getMsg(err))
            if(err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors as List<Violation>
    }

    //Unique index unique constraint or primary key violation
    @SuppressWarnings('BracesForIfElse')
    static String isUniqueIndexViolation(DataAccessException dax){
        String rootMessage = dax.rootCause.message
        if(rootMessage.contains("Unique index or primary key violation") || //mysql and H2
            rootMessage.contains("Duplicate entry") || //mysql
            rootMessage.contains("Violation of UNIQUE KEY constraint") || //sql server
            rootMessage.contains("unique constraint"))
        {
            return rootMessage
        } else {
            return null
        }
    }
}
