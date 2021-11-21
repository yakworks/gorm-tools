/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

import yakworks.problem.exception.NestedProblemException

/**
 * Throwable Exception Problem
 */
@CompileStatic
class OptimisticLockingProblem extends NestedProblemException
    implements DataProblemTrait<OptimisticLockingProblem> {

    String defaultCode = 'error.optimisticLocking'

    OptimisticLockingProblem(Throwable cause) {
        super(cause)
    }

}
