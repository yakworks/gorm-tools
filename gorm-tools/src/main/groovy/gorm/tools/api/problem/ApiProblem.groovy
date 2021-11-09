/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api.problem

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import org.springframework.http.HttpStatus

import gorm.tools.support.MsgSourceResolvable

/**
 * Default problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Builder(builderStrategy= SimpleStrategy, prefix="") @TupleConstructor
@ToString @EqualsAndHashCode
@CompileStatic
class ApiProblem implements Problem, MsgSourceResolvable, Serializable {
    //reimplement Problem fields so @Builder will pick them up, easier to do this than to manually implement builder methods
    Integer status = 400; String title; String detail;

    final Boolean ok = false

    static ApiProblem of(String code, List args = null){
        def prob = new ApiProblem()
        prob.setMessage(code, args)
        return prob
    }

    static ApiProblem of(Integer statusId){
        new ApiProblem(status: statusId)
    }

    static ApiProblem of(HttpStatus httpStatus){
        new ApiProblem(status: httpStatus.value(), title: httpStatus.reasonPhrase)
    }

    static ApiProblem of(HttpStatus httpStatus, String title, String detail = null){
        new ApiProblem(status: httpStatus.value(), title: title, detail: detail)
    }

    ApiProblem status(HttpStatus httpStatus){
        this.status = httpStatus.value()
        return this
    }

}
