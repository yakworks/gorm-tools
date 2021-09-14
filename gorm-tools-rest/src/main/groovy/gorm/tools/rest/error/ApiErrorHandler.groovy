/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.error

import groovy.transform.CompileStatic

import org.springframework.dao.DataAccessException

import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

/**
 * Prepares ApiError / ApiValidationError for given exception
 */
@CompileStatic
class ApiErrorHandler {

    static ApiError handleException(Class entityClass, Exception e) {
        if (e instanceof EntityNotFoundException) {
            return new ApiError(status: NOT_FOUND, title: "Not Found", detail: e.message)
        } else if (e instanceof EntityValidationException) {
            //e.message is full error message with msg for each field, so use e.defaultMessage
            //FIXME #339 EntityValidationException is a msgSource, pass it through messageSource.getMessage
            //defaultMessage wont always be there, sometimes we just pass in the key and no default desc
            return new ApiValidationError(e.defaultMessage, e.errors)
        } else if (e instanceof ValidationException) {
            //e.message will be full error message, so build same error message as EntityValidationException.defaultMessage
            return new ApiValidationError("${entityClass.simpleName} validation errors", e.errors)
        } else if (e instanceof DataAccessException) {
            return new ApiError(status: UNPROCESSABLE_ENTITY, title: "Data Access Exception", detail: e.message)
        } else {
            return new ApiError(status: INTERNAL_SERVER_ERROR, title: "Internal Error", detail: e.message)
        }
    }
}
