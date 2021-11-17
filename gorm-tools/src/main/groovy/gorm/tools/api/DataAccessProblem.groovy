/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic

import org.springframework.dao.NonTransientDataAccessException

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.problem.Exceptional
import yakworks.api.problem.ProblemTrait
import yakworks.api.problem.RuntimeProblem

/**
 * Root Problem for spring data access
 */
@CompileStatic
abstract class DataAccessProblem<E> extends NonTransientDataAccessException implements ProblemTrait<E>, Exceptional {
    ApiStatus status = HttpStatus.BAD_REQUEST

    protected DataAccessProblem(String msg) {
        super(msg)
    }
    protected DataAccessProblem(String msg, Throwable cause) {
        super(msg, cause)
    }

    protected DataAccessProblem() {
        super(null)
    }

    @Override //throwable
    String getMessage() {
        return RuntimeProblem.buildMessage(this)
    }

    @Override
    String toString() {
        return RuntimeProblem.buildToString(this)
    }

    @Override
    DataAccessProblem getCause() {
        // cast is safe, since the only way to set this is our constructor
        return (DataAccessProblem) super.getCause()
    }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    @SuppressWarnings(['SynchronizedMethod'])
    @Override
    synchronized Throwable fillInStackTrace() { return this }
}
