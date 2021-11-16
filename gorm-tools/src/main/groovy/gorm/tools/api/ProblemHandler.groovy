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
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.support.MsgService
import gorm.tools.support.MsgSourceResolvable
import grails.validation.ValidationException
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.problem.ApiProblem
import yakworks.api.problem.Problem
import yakworks.api.problem.ProblemTrait
import yakworks.api.problem.ValidationProblem
import yakworks.api.problem.Violation
import yakworks.api.problem.ViolationFieldError
import yakworks.i18n.MsgKey

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
        Integer statusId = UNPROCESSABLE_ENTITY.value()

        if (e instanceof EntityNotFoundException) {
            // return Problem.of(status404).msg(MsgKey.of('error.notFound')).detail(e.message)
            return ApiProblem.of(NOT_FOUND.value(), getMsg(e)).msg(MsgKey.of('error.notFound'))
        }
        else if (e instanceof EntityValidationException) {
            String detail
            if(e.errors instanceof EmptyErrors){
                //this is some other exception wrapped in validation exception
                detail = e.cause?.message
            }
            return ValidationProblem.of(status422, getMsg(e), detail).errors(toFieldErrorList(e.errors))
        }
        else if (e instanceof ValidationException) {
            String msg = 'Validation Error'
            return ValidationProblem.of(status422, msg, e.message).errors(toFieldErrorList(e.errors))
        }
        else if (e instanceof MsgSourceResolvable) { //legacy
            return Problem.of(status400).msg(MsgKey.of(e.code)).detail(getMsg(e))
        }
        else if (e instanceof IllegalArgumentException) {
            //We use this all over to double as a validation error, Validate.notNull for example.
            return Problem.of(status400).msg(MsgKey.of('error.illegalArgument')).detail(e.message)
        }
        else if (e instanceof DataAccessException) {
            log.error("UNEXPECTED Data Access Exception ${e.message}", e)
            return Problem.of(status400).msg(MsgKey.of('error.dataAccess')).detail(e.message)
        }
        else {
            log.error("UNEXPECTED Internal Server Error ${e.message}", e)
            return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR).msg(MsgKey.of('error.unhandled')).detail(e.message)
        }
    }

    String getMsg(MessageSourceResolvable msr){
        String msg = msgService.getMessage(msr)
        return msg
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    //FIXME #339 see errormessageService, do we need some of that logic?
    List<Violation> toFieldErrorList(Errors errs) {
        List<ViolationFieldError> errors = []
        for(ObjectError err : errs.allErrors) {
            ViolationFieldError fieldError = ViolationFieldError.of(err.code, getMsg(err))
            if(err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors as List<Violation>
    }
}
