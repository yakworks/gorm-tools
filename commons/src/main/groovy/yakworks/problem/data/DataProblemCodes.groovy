/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

/**
 * Enum helper for codes
 */
@CompileStatic
enum DataProblemCodes {

    NotFound('error.notFound'),
    OptimisticLocking('error.data.optimisticLocking'),
    ReferenceKey('error.data.reference'),
    UniqueConstraint('error.data.uniqueConstraintViolation')

    final String code

    DataProblemCodes(String code) {
        this.code = code
    }

    DataProblem get(){
        new DataProblem().msg(code)
    }

    DataProblem withArgs(Map args){
        new DataProblem().msg(code, args)
    }

    DataProblem ofCause(Throwable cause){
        return DataProblem.ofCause(cause).msg(code)
    }

}
