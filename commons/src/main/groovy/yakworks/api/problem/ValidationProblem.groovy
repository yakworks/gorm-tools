/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import groovy.transform.CompileStatic

/**
 * For validation/violation and contraint errors where we need to report on fields
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ValidationProblem implements ProblemTrait<ValidationProblem>, Serializable {

    //errors default val empty list
    List<ProblemFieldError> errors = []

    static ValidationProblem of(int statusId){
        new ValidationProblem().status(statusId)
    }

    static ValidationProblem of(Integer statusId, String title, String detail = null){
        new ValidationProblem(status: statusId, title: title, detail: detail)
    }

}
