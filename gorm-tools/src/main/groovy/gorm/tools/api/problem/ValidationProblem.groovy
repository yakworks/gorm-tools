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
 * For validation/violation and contraint errors where we need to report on fields
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Builder(builderStrategy= SimpleStrategy, prefix="") @TupleConstructor
@ToString @EqualsAndHashCode
@CompileStatic
class ValidationProblem implements Problem, Serializable {
    //reimplement Problem fields so builder and ann can pick them up, easier to this than to redo builder
    int status; String title; String detail

    //errors default val empty list
    List<ProblemFieldError> errors = []

    static ValidationProblem of(int statusId){
        new ValidationProblem(statusId)
    }

    static ValidationProblem of(HttpStatus httpStatus){
        new ValidationProblem(status: httpStatus.value(), title: httpStatus.reasonPhrase)
    }

    static ValidationProblem of(HttpStatus httpStatus, String title, String detail = null){
        new ValidationProblem(status: httpStatus.value(), title: title, detail: detail)
    }

    ValidationProblem status(HttpStatus httpStatus){
        this.status = httpStatus.value()
        return this
    }

}
