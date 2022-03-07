/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus

/**
 * Concrete problem for unexpected exceptions or untrapped that can be called as a flow through
 * These can get special andling and alerts in logging as , well , they should not have happened
 * and deserve attention as it means code is fubarred.
 */
@CompileStatic
class UnexpectedProblem implements ProblemTrait<UnexpectedProblem> {
    public static String DEFAULT_CODE = 'error.unexpected'
    String defaultCode = DEFAULT_CODE
    ApiStatus status = HttpStatus.INTERNAL_SERVER_ERROR

    static ProblemException ex(String message){
        return Problem.ofCode(DEFAULT_CODE).detail(message).toException()
    }
}
