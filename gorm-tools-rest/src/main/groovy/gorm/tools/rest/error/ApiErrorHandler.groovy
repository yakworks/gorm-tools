/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.error

import groovy.transform.CompileStatic

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.dao.DataAccessException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.beans.AppCtx
import gorm.tools.repository.RepoMessage
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

/**
 * Prepares ApiError / ApiValidationError for given exception
 */
//FIXME #339 move to gorm.tools.api so it can be shared if we need it
@CompileStatic
class ApiErrorHandler {

    static ApiError handleException(Class entityClass, Exception e) {
        if (e instanceof EntityNotFoundException) {
            return new ApiError(status: NOT_FOUND, title: "Not Found", detail: e.message)
        } else if (e instanceof EntityValidationException) {
            //FIXME #339 these dont need to be static, make this a bean and use support.MsgService
            return new ApiValidationError(AppCtx.getCtx().getMessage(e,  RepoMessage.defaultLocale()), toFieldErrorList(e.errors))
        } else if (e instanceof ValidationException) {
            //e.message will be full error message, so build same error message as EntityValidationException.defaultMessage
            return new ApiValidationError("${entityClass.simpleName} validation errors", toFieldErrorList(e.errors))
        } else if (e instanceof DataAccessException) {
            return new ApiError(status: UNPROCESSABLE_ENTITY, title: "Data Access Exception", detail: e.message)
        } else {
            return new ApiError(status: INTERNAL_SERVER_ERROR, title: "Internal Error", detail: e.message)
        }
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    static List<ApiFieldError> toFieldErrorList(Errors errs) {
        List<ApiFieldError> errors = []
        MessageSource messageSource =  AppCtx.getCtx()
        errs.allErrors.each {def err ->
            ApiFieldError fieldError = new ApiFieldError()
            // make this a bean and use MsgService
            fieldError.message = messageSource.getMessage(err, RepoMessage.defaultLocale())
            if(err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors
    }
}
