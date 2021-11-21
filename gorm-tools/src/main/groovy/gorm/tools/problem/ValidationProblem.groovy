/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.problem

import groovy.transform.CompileStatic

import org.springframework.validation.Errors
import org.springframework.validation.ObjectError

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.i18n.MsgKey
import yakworks.problem.data.DataProblemTrait
import yakworks.problem.exception.NestedProblemException

/**
 * an extension of the default ValidationException so you can pass the entity and the message source
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class ValidationProblem extends NestedProblemException
    implements DataProblemTrait<ValidationProblem> {

    public static String DEFAULT_CODE ='validation.problem'
    public static String DEFAULT_TITLE ='Validation Error(s)'

    Errors errors

    //overrides
    String defaultCode = DEFAULT_CODE
    String title = DEFAULT_TITLE
    ApiStatus status = HttpStatus.UNPROCESSABLE_ENTITY

    ValidationProblem() {
        super()
    }

    ValidationProblem(String message) {
        super(message)
        detail(message)
    }

    ValidationProblem(Throwable cause) {
        super(cause)
        detail(cause.message)
    }

    ValidationProblem errors(Errors v) {this.errors = v; return this;}

    ValidationProblem name(String nm){
        args.putIfAbsent('name', nm)
        return this
    }

    //Legacy from ValidationException
    static String formatErrors(Errors errors, String msg ) {
        String ls = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        if (msg != null) {
            b.append(msg).append(" : ").append(ls);
        }

        for (ObjectError error : errors.getAllErrors()) {
            b.append(ls)
                .append(" - ")
                .append(error)
                .append(ls);
        }
        return b.toString();
    }

    static ValidationProblem of(Object entity, Throwable cause) {
        return ValidationProblem.cause(cause).entity(entity);
    }
}
