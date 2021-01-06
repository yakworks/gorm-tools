/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

import org.springframework.dao.DataRetrievalFailureException

import gorm.tools.support.MsgSourceResolvable

/**
 * an extension of the DataRetrievalFailureException that is more performant. fillInStackTrace is overriden to show nothing
 * so it will be faster and consume less memory when thrown.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class EntityNotFoundException extends DataRetrievalFailureException implements MsgSourceResolvable {

    EntityNotFoundException(String msg) {
        super(msg)
        defaultMessage = msg
    }

    EntityNotFoundException(Serializable id, String domainName) {
        this('default.not.found.message',
            [domainName, (id instanceof Number ? "id:$id" : id.toString())],
            "${domainName} not found for ${id instanceof Number ? "id:$id" : id.toString()}"
        )
    }

    EntityNotFoundException(String code, List args, String defaultMsg = null) {
        super(defaultMsg ?: code)
        setMessage(code, args, defaultMsg)
    }

    EntityNotFoundException(MsgSourceResolvable msgKey) {
        super(msgKey.defaultMessage ?: msgKey.code)
        setMessage(msgKey)
    }

    /**
     * Constructor for DataRetrievalFailureException.
     * @param msg the detail message
     * @param cause the root cause from the data access API in use
     */
    EntityNotFoundException(String msg, Throwable cause) {
        super(msg, cause)
    }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    @SuppressWarnings(['SynchronizedMethod'])
    @Override
    synchronized Throwable fillInStackTrace() { return this }
}
