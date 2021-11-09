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

/**
 * Default problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Builder(builderStrategy= SimpleStrategy, prefix="") @TupleConstructor
@ToString @EqualsAndHashCode
@CompileStatic
class DefaultProblem implements Problem, Serializable {
    //reimplement Problem fields so @Builder will pick them up, easier to do this than to redo builder methods
    int status; String title; String detail; String code

    static DefaultProblem of(int statusId){
        new DefaultProblem(status: statusId)
    }

    static DefaultProblem of(HttpStatus httpStatus){
        new DefaultProblem(status: httpStatus.value(), title: httpStatus.reasonPhrase)
    }

    static DefaultProblem of(HttpStatus httpStatus, String title, String detail = null){
        new DefaultProblem(status: httpStatus.value(), title: title, detail: detail)
    }

    DefaultProblem status(HttpStatus httpStatus){
        this.status = httpStatus.value()
        return this
    }

}
