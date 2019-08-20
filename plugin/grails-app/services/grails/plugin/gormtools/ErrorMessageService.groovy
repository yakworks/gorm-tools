/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.gormtools

import java.sql.BatchUpdateException
import javax.persistence.PersistenceException

import groovy.transform.CompileDynamic

import org.hibernate.exception.ConstraintViolationException
import org.springframework.context.MessageSource

import gorm.tools.repository.RepoMessage
import grails.util.GrailsNameUtils
import grails.validation.ValidationException

@CompileDynamic
class ErrorMessageService {

    MessageSource messageSource

    /**
     * Builds the error response from error to make it more human readable.
     * Used in BaseDomain controller and ArTranMassUpdateService, to show all errors that occurred during processing
     *
     * @param e exception object
     * @return map with next fields
     *      code - HTTP response code
     *      message - text message of the error
     *      messageCode - code of the error
     *      errors - list of errors for each entity field
     */
    Map buildErrorResponse(Exception e) {
        int code = 500
        Throwable curr = e
        // when constraint is on db side, for example dup key, the exception is fired on flush and wrapped into PersistenceException
        if (e instanceof PersistenceException ){
            e = e.cause
        }

        if (curr instanceof ValidationException || curr instanceof ConstraintViolationException
            || curr instanceof org.grails.datastore.mapping.validation.ValidationException) {
            code = 422
        }

        List<Throwable> causes = []
        while (curr?.cause != null) {
            causes << curr.cause
            curr = curr.cause
        }

        Map errMap = [
            "code"       : code,
            "status"     : "error",
            "message"    : curr.hasProperty('messageMap') ? buildMsg(curr.messageMap) : curr.message,
            "messageCode": curr.hasProperty('messageMap') ? curr.messageMap.code : 0,
            "errors"     : [:]
        ]

        if (e.hasProperty('errors')) {
            if (e.hasProperty("entity") && e.entity?.errors) {
                errMap.errors = e.entity.errors.fieldErrors.groupBy {
                    GrailsNameUtils.getPropertyNameRepresentation(it.objectName)
                }.each {
                    it.value = it.value.collectEntries {
                        [(it.field): messageSource.getMessage(it, Locale.ENGLISH)]
                    }
                }
            } else if (!e.hasProperty("entity")) {
                errMap.errors = e.errors.fieldErrors.groupBy {
                    GrailsNameUtils.getPropertyNameRepresentation(it.objectName)
                }.each {
                    it.value = it.value.collectEntries {
                        [(it.field): messageSource.getMessage(it, Locale.ENGLISH)]
                    }
                }
            }
        }

        if (e.hasProperty('entity')) {
            BatchUpdateException core = causes.find { it instanceof BatchUpdateException }
            String num = e.entity.hasProperty('num') ? e.entity.num : null
            if (core && core.message.startsWith('Duplicate entry') && num && core.message.contains(num)) {
                errMap.errors = errMap.errors ?: [:]
                errMap.errors.num = 'Duplicate entry'
            }
        }
        return errMap
    }

    String buildMsg(Map msgMap) {
        Object[] args = (msgMap.args instanceof List) ? msgMap.args as Object[] : [] as Object[]

        return messageSource.getMessage(msgMap.code, args, msgMap.defaultMessage, RepoMessage.defaultLocale())
    }
}
