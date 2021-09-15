/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.error

import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus
import org.springframework.validation.Errors

import gorm.tools.repository.errors.RepoExceptionSupport

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

/**
 * a validation error based on https://datatracker.ietf.org/doc/html/rfc7807
 */
@CompileStatic
class ApiValidationError extends ApiError {
    List<ApiFieldError> errors = []

    ApiValidationError(HttpStatus status, String title, String detail, List<ApiFieldError> errs) {
        this.status = status
        this.title = title
        this.detail = detail
        this.errors = errs
    }

    ApiValidationError(String detail, List<ApiFieldError> errs) {
        this(UNPROCESSABLE_ENTITY, "Validation Error", detail, errs)
    }
}
