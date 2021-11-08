/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api.problem

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSourceResolvable
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import gorm.tools.repository.RepoMessage
import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.support.MsgService
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

/**
 * Service to prepare ApiError / ApiValidationError for given exception
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Slf4j
@CompileStatic
class ProblemHandler {

    @Autowired
    MsgService msgService

    Problem handleException(Throwable e) {
        handleException("Entity", e)
    }

    Problem handleException(Class entityClass, Throwable e) {
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
    Problem handleException(String entityName, Throwable e) {
        // default error status code is 422
        HttpStatus status = UNPROCESSABLE_ENTITY

        if (e instanceof EntityNotFoundException) {
            return DefaultProblem.of(NOT_FOUND, getMsg(e))
        }
        else if (e instanceof EntityValidationException) {
            String detail
            if(e.errors instanceof EmptyErrors){
                //this is some other exception wrapped in validation exception
                detail = e.cause?.message
            }
            return ValidationProblem.of(status, getMsg(e), detail).errors(toFieldErrorList(e.errors))
        }
        else if (e instanceof ValidationException) {
            // grails has a ValidationException, so does gorm
            def msg = getMsg(RepoMessage.validationError(entityName))
            return ValidationProblem.of(status, msg).errors(toFieldErrorList(e.errors))
        }
        else if (e instanceof MessageSourceResolvable) {
            return DefaultProblem.of(status, getMsg(e))
        }
        else if (e instanceof IllegalArgumentException) {
            //We use this all over to double as a validation error, Validate.notNull for example.
            return DefaultProblem.of(status, "Illegal Argument", e.message)
        }
        else if (e instanceof DataAccessException) {
            log.error("UNEXPECTED Data Access Exception ${e.message}", e)
            return DefaultProblem.of(status, "Data Access Exception", e.message)
        }
        else {
            log.error("UNEXPECTED Internal Server Error ${e.message}", e)
            return DefaultProblem.of(INTERNAL_SERVER_ERROR).detail(e.message)
        }
    }

    String getMsg(MessageSourceResolvable msr){
        msgService.getMessage(msr)
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    //FIXME #339 see errormessageService, do we need some of that logic?
    List<ProblemFieldError> toFieldErrorList(Errors errs) {
        List<ProblemFieldError> errors = []
        for(ObjectError err : errs.allErrors) {
            ProblemFieldError fieldError = new ProblemFieldError(getMsg(err))
            if(err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors
    }
}