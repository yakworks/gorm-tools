/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.validation.ValidationException
import org.springframework.core.NestedRuntimeException
import org.springframework.validation.Errors

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.support.MsgKey
import gorm.tools.support.MsgSourceResolvable

/**
 * A general RuntimeException that uses Message Source Keys to indicate the error
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.x
 */
@CompileStatic
class MsgKeyException extends NestedRuntimeException implements MsgSourceResolvable {

    Object target //a target object that may want to associate

    MsgKeyException(String code, List arguments, Throwable cause = null) {
        super(code)
        if(cause) initCause(cause)
        this.msgCodes = [code]
        this.args = arguments
    }

    MsgKeyException(String defaultMessage, Throwable cause = null) {
        super(defaultMessage)
        if(cause) initCause(cause)
        this.defaultMessage = defaultMessage
    }

    MsgKeyException(MsgKey msgKey, Throwable cause = null) {
        super(msgKey.code)
        if(cause) initCause(cause)
        setMessage(msgKey)
    }

}
