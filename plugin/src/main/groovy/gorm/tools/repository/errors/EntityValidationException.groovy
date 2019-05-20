/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.validation.ValidationException
import org.springframework.validation.Errors

/**
 * an extension of the default ValidationException so you can pass the entity and the message map
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class EntityValidationException extends ValidationException {

    Object entity //the entity that the error occured on
    Map meta //any meta that can be set and passed up the chain for an error
    Map messageMap //map with message info code,orgs and defaultMessage
    Object otherEntity //another entity on which error occurred

    EntityValidationException(String msg) {
        super(msg, new EmptyErrors("empty"))
        //messageMap = [code:"validationException", args:[], defaultMessage:msg]
    }

    EntityValidationException(String msg, Errors e) {
        this(msg, e, null)
    }

    EntityValidationException(String msg, Errors e, Throwable cause) {
        super(msg, e)
        initCause(cause)
        messageMap = [code: "validationException", args: [], defaultMessage: msg]
    }

    EntityValidationException(Map msgMap, Object entity, Errors errors) {
        this(msgMap, entity, errors, null)
    }

    EntityValidationException(Map msgMap, Object entity) {
        this(msgMap, entity, null, null)
    }

    EntityValidationException(Map msgMap, Object entity, Throwable cause) {
        this(msgMap, entity, null, cause)
    }

    EntityValidationException(Map msgMap, Object entity, Errors errors, Throwable cause) {
        super(msgMap.defaultMessage?.toString() ?: "Save or Validation Error(s) occurred", errors ?: new EmptyErrors("empty"))
        initCause(cause)
        this.messageMap = msgMap
        this.entity = entity
        messageMap.defaultMessage = messageMap.defaultMessage ?: "Save or Validation Error(s) occurred"
    }

}
