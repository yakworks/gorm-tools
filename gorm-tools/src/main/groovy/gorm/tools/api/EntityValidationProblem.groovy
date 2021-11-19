/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic

import org.springframework.validation.Errors
import org.springframework.validation.ObjectError

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.i18n.MsgKey

import static java.util.Arrays.asList
import static yakworks.problem.spi.StackTraceProcessor.COMPOUND

/**
 * an extension of the default ValidationException so you can pass the entity and the message source
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class EntityValidationProblem extends AbstractDataAccessProblem<EntityValidationProblem>   {
    public static String DEFAULT_CODE ='validation.problem'
    public static String DEFAULT_TITLE ='Validation Error(s)'

    Errors errors

    //overrides
    String defaultCode = DEFAULT_CODE
    String title = DEFAULT_TITLE
    ApiStatus status = HttpStatus.UNPROCESSABLE_ENTITY

    EntityValidationProblem() {
        super('')
    }

    EntityValidationProblem(String message) {
        super(message)
        detail(message)
    }

    EntityValidationProblem(Throwable cause) {
        super('', cause)
        detail(cause.message)
    }


    // EntityValidationProblem(final EntityValidationProblem cause) {
    //     super(DEFAULT_CODE, cause)
    //     final Collection<StackTraceElement> stackTrace = COMPOUND.process(asList(getStackTrace()))
    //     setStackTrace(stackTrace as StackTraceElement[])
    // }

    EntityValidationProblem errors(Errors v) {this.errors = v; return this;}

    EntityValidationProblem name(String nm){
        putArgIfAbsent('name', nm)
        return this
    }

    //Legacy from ValidationException
    public static String formatErrors(Errors errors, String msg ) {
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

    static EntityValidationProblem of(MsgKey msg) {
        return new EntityValidationProblem().msg(msg)
    }

    static EntityValidationProblem of(final Throwable cause) {
        return new EntityValidationProblem(cause);
    }

    static EntityValidationProblem of(Object entity, Throwable cause) {
        return EntityValidationProblem.of(cause).entity(entity);
    }
}
