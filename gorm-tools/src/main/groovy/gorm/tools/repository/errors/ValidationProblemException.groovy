/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

import org.springframework.dao.DataIntegrityViolationException

import yakworks.i18n.MsgKeyTrait

/**
 * an extension of the default ValidationException so you can pass the entity and the message source
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class ValidationProblemException extends DataIntegrityViolationException implements MsgKeyTrait {
    Object entity //the entity that the error occured on

    public ValidationProblemException(String msg) {
        super(msg);
    }

    /**
     * Constructor for DataIntegrityViolationException.
     * @param msg the detail message
     * @param cause the root cause from the data access API in use
     */
    public ValidationProblemException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
