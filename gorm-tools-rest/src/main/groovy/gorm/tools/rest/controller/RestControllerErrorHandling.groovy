/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import groovy.transform.CompileDynamic

import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.repository.RepoMessage
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.CONFLICT
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

/**
 *  Adds controller error handlers
 *
 *  Created by alexeyzvegintcev.
 */
@CompileDynamic
trait RestControllerErrorHandling {

    void handleException(EntityNotFoundException e) {
        log.info e.message
        render(status: NOT_FOUND, e.message)
    }

    void handleException(EntityValidationException e) {
        String m = buildMsg(e.messageMap, e.errors)
        log.info m
        render(status: UNPROCESSABLE_ENTITY, m)
    }

    void handleException(ValidationException e) {
        String m = buildMsg([defaultMessage: e.message], e.errors)
        log.info m
        render(status: UNPROCESSABLE_ENTITY, m)
    }

    void handleException(OptimisticLockingFailureException e) {
        log.info e.message
        render(status: CONFLICT, e.message)
    }

    void handleException(RuntimeException e) {
        log.error e.message
        throw e
    }

    String buildMsg(Map msgMap, Errors errors) {
        StringBuilder result = new StringBuilder(msgMap.defaultMessage)
        errors.getAllErrors().each { FieldError error ->
            result.append("\n" + message(error: error, args: error.arguments, local: RepoMessage.defaultLocale()))
        }
        return result
    }
}
