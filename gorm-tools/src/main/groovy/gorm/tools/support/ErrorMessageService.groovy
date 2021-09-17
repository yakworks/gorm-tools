/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import java.sql.BatchUpdateException
import javax.inject.Inject
import javax.persistence.PersistenceException

import groovy.transform.CompileDynamic

import org.hibernate.exception.ConstraintViolationException
import org.springframework.context.MessageSource

import grails.validation.ValidationException
import yakworks.commons.lang.NameUtils

/**
 * creates a Map from errors so it can be converted to json and sent to client
 */
//FIXME #339 is any of the functionality needed here? do we capture the BatchUpdateException still
// if we replaced the functionality of this then lets remove it
@CompileDynamic
class ErrorMessageService {

    @Inject
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

        if (e instanceof PersistenceException ){
            e = e.cause
        }

        if (e instanceof ValidationException || e instanceof ConstraintViolationException
            || e instanceof org.grails.datastore.mapping.validation.ValidationException) {
            code = 422
        }

        List<Throwable> causes = []
        Throwable curr = e
        while (curr?.cause != null) {
            causes << curr.cause
            curr = curr.cause
        }
        String message = e.hasProperty('messageMap') ? buildMsg(e.messageMap) : e.message
        Map errMap = [
            "code"       : code,
            "status"     : "error",
            // Persistence exception hides real reason with `could not execute statement`
            "message"    : message == "could not execute statement" && curr?.message ? curr.message: message,
            "messageCode": e.hasProperty('messageMap') ? e.messageMap.code : 0,
            "errors"     : [:]
        ]

        if (e.hasProperty('errors')) {
            if (e.hasProperty("entity") && e.entity?.errors) {
                errMap.errors = e.entity.errors.fieldErrors.groupBy {
                    NameUtils.getPropertyNameRepresentation(it.objectName)
                }.each {
                    it.value = it.value.collectEntries {
                        [(it.field): messageSource.getMessage(it, Locale.ENGLISH)]
                    }
                }
            } else if (!e.hasProperty("entity")) {
                errMap.errors = e.errors.fieldErrors.groupBy {
                    NameUtils.getPropertyNameRepresentation(it.objectName)
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

        return messageSource.getMessage(msgMap.code, args, msgMap.defaultMessage, LocaleContextHolder.getLocale())
    }
}
