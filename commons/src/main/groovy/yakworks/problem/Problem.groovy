/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.ResultTrait
import yakworks.i18n.MsgKey

/**
 * Default problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class Problem implements ResultTrait<Problem>, ProblemTrait<Problem> {

    Problem(){}

    Problem(ApiStatus v){ this.status = v }

    Problem(MsgKey mk){ this.msg = mk }

    class Exception extends RuntimeException {

        Problem getProblem(){
            return Problem.this
        }
    }

    Problem.Exception toThrowable(){
        return new Problem.Exception();
    }

}
