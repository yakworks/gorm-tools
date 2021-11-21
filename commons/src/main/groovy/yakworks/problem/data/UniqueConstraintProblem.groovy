/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.problem.exception.NestedProblemException

/**
 * generic problem
 */
@CompileStatic
class UniqueConstraintProblem extends NestedProblemException
    implements DataProblemTrait<UniqueConstraintProblem>  {

    String defaultCode = 'error.uniqueConstraintViolation'

    UniqueConstraintProblem() {
        super()
    }

    UniqueConstraintProblem(Throwable cause) {
        super(cause)
    }

}
