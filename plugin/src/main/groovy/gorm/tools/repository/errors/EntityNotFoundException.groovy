/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

import org.springframework.dao.DataRetrievalFailureException

/**
 * an extension of the DataRetrievalFailureException that is more performant. fillInStackTrace is overriden to show nothing
 * so it will be faster and consume less memory when thrown.
 */
@CompileStatic
class EntityNotFoundException extends DataRetrievalFailureException {

    EntityNotFoundException(String msg) {
        super(msg)
    }

    EntityNotFoundException(Serializable id, String domainName) {
        super("${domainName} not found with id ${id}")
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
    synchronized Throwable fillInStackTrace() {}
}
