/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.error

import groovy.transform.CompileStatic

import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.beans.AppCtx
import gorm.tools.repository.RepoMessage
import grails.validation.ValidationException


@CompileStatic
class ApiValidationError extends ApiError {
    List<Map> errors = []

    ApiValidationError(HttpStatus status, String title, String detail, Errors errs) {
        this.status = status
        this.title = title
        this.detail = detail
        populateErrors(errs)
    }

    private void populateErrors(Errors errs) {
        MessageSource messageSource =  AppCtx.getCtx()
        errs.allErrors.each {def err
            Map m = [message:messageSource.getMessage(it, RepoMessage.defaultLocale())]
            if(it instanceof FieldError) m['field'] = it.field
            this.errors << m
        }
    }
}
