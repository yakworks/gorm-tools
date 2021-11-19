/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic

import org.springframework.dao.NonTransientDataAccessException

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.problem.Exceptional
import yakworks.problem.ProblemException
import yakworks.problem.ProblemTrait

/**
 * Root Problem for spring data access
 */
@CompileStatic
abstract class AbstractDataAccessProblem<E extends AbstractDataAccessProblem>
    extends NonTransientDataAccessException
    implements ProblemTrait<E>, Exceptional {

    ApiStatus status = HttpStatus.BAD_REQUEST

    //additional
    Object entity //the entity that the error occured on
    boolean writableStackTrace = false

    protected AbstractDataAccessProblem(String msg) {
        this(msg, null)
    }
    protected AbstractDataAccessProblem(String msg, Throwable cause) {
        super(msg, cause)
    }

    protected AbstractDataAccessProblem() {
        this('', null)
    }

    E entity(Object v) {
        if(v != null) {
            this.entity = v
            ProblemUtils.addCommonArgs(getArgMap(), v)
        }
        return (E)this;
    }

    @Override //throwable
    String getMessage() {
        return ProblemException.buildMessage(this)
    }

    @Override
    String toString() {
        return ProblemException.buildToString(this)
    }

    @Override
    // AbstractDataAccessProblem getCause() {
    //     // cast is safe, since the only way to set this is our constructor
    //     return (AbstractDataAccessProblem) super.getCause()
    // }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    /**
     * By default this is called in Throwable constructor
     * for performance improvement Override to disable by default.
     * to turn it back on call fillInStackTraceSuper
     */
    @SuppressWarnings(['SynchronizedMethod'])
    @Override
    synchronized Throwable fillInStackTrace() { return this }

    @SuppressWarnings(['SynchronizedMethod', 'BracesForIfElse'])
    synchronized Throwable fillInStackTraceSuper() { return super.fillInStackTrace() }
}
