/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus

/**
 * generic problem
 */
@CompileStatic
class UniqueConstraintProblem extends AbstractDataAccessProblem<UniqueConstraintProblem> {
    public static String DEFAULT_CODE = 'error.uniqueConstraintViolation'
    String defaultCode = DEFAULT_CODE
    ApiStatus status = HttpStatus.BAD_REQUEST

    protected UniqueConstraintProblem() {
        super(DEFAULT_CODE)
    }

    protected UniqueConstraintProblem(Throwable cause) {
        super(DEFAULT_CODE, cause)
    }

    static UniqueConstraintProblem of(final Throwable cause) {
        def dap = new UniqueConstraintProblem(cause)
        dap.detail(dap.rootCause.message)
    }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    // @SuppressWarnings(['SynchronizedMethod'])
    // @Override
    // synchronized Throwable fillInStackTrace() { return this }
}
