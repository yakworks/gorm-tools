/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

/**
 * generic problem
 */
@CompileStatic
class DataAccessProblem extends AbstractDataAccessProblem<DataAccessProblem> {
    public static String DEFAULT_CODE = 'error.dataAccessException'
    String defaultCode = DEFAULT_CODE

    protected DataAccessProblem() {
        super(DEFAULT_CODE)
    }

    protected DataAccessProblem(Throwable cause) {
        super(DEFAULT_CODE, cause)
    }

    static DataAccessProblem of(final Throwable cause) {
        def dap = new DataAccessProblem(cause)
        dap.detail(dap.rootCause.message)
    }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    // @SuppressWarnings(['SynchronizedMethod'])
    // @Override
    // synchronized Throwable fillInStackTrace() { return this }
}
