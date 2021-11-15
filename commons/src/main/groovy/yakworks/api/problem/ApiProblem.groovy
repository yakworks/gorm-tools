/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus

/**
 * Default problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ApiProblem implements ProblemTrait {

    static ApiProblem of(Integer statusId){
        return (ApiProblem) new ApiProblem().status(statusId)
    }

    static ApiProblem of(ApiStatus status){
        return new ApiProblem(status: status)
    }

    static ProblemTrait of(Integer statusId, String title, String detail = null){
        return (ApiProblem) new ApiProblem(title: title, detail: detail).status(statusId)
    }



}
