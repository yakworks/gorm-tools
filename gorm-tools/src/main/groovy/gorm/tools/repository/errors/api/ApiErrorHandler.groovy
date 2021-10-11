/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors.api

import groovy.transform.CompileStatic

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
 * Prepares ApiError / ApiValidationError for given exception
 */
@CompileStatic
class ApiErrorHandler {

    @Autowired MsgService msgService

    ApiError handleException(Throwable e) {
        handleException("Entity", e)
    }

    ApiError handleException(Class entityClass, Throwable e) {
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
    ApiError handleException(String entityName, Throwable e) {
        // default error status code is 422
        HttpStatus status = UNPROCESSABLE_ENTITY

        if (e instanceof EntityNotFoundException) {
            return new ApiError(NOT_FOUND, getMsg(e))
        }
        else if (e instanceof EntityValidationException) {
            String detail
            if(e.errors instanceof EmptyErrors){
                //this is some other exception wrapped in validation exception
                detail = e.cause?.message
            }
            return new ApiError(status, getMsg(e), detail).errors(toFieldErrorList(e.errors))
        }
        else if (e instanceof ValidationException) {
            // grails has a ValidationException, so does gorm
            def msg = getMsg(RepoMessage.validationError(entityName))
            return new ApiError(status, msg).errors(toFieldErrorList(e.errors))
        }
        else if (e instanceof MessageSourceResolvable) {
            return new ApiError(status, getMsg(e))
        }
        else if (e instanceof DataAccessException) {
            return new ApiError(status, "Data Access Exception").detail(e.message)
        }
        else {
            return new ApiError(INTERNAL_SERVER_ERROR, "Internal Server Error", e.message)
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
    List<ApiFieldError> toFieldErrorList(Errors errs) {
        List<ApiFieldError> errors = []
        for(ObjectError err : errs.allErrors) {
            ApiFieldError fieldError = new ApiFieldError(getMsg(err))
            if(err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors
    }
}
