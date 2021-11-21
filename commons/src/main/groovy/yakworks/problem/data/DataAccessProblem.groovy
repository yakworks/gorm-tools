/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

import yakworks.problem.exception.NestedProblemException

/**
 * generic problem
 */
@CompileStatic
class DataAccessProblem extends NestedProblemException implements DataProblemTrait<DataAccessProblem>  {
    String defaultCode = 'error.dataAccess'

    DataAccessProblem() {
        super()
    }

    DataAccessProblem(Throwable cause) {
        super(cause)
    }
    //
    // static DataAccessProblem cause(final Throwable cause) {
    //     def dap = new DataAccessProblem(cause)
    //     dap.detail(dap.rootCause.message)
    // }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    // @SuppressWarnings(['SynchronizedMethod'])
    // @Override
    // synchronized Throwable fillInStackTrace() { return this }
}
