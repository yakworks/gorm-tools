/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import groovy.transform.CompileStatic

/**
 * Default problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ApiProblem implements ProblemTrait<ApiProblem> {

    static ApiProblem of(Integer statusId){
        new ApiProblem(status: statusId)
    }

    // static ApiProblem of(HttpStatus httpStatus){
    //     new ApiProblem(status: httpStatus.value(), title: httpStatus.reasonPhrase)
    // }
    //
    static ApiProblem of(Integer statusId, String title, String detail = null){
        new ApiProblem(status: statusId, title: title, detail: detail)
    }



}
