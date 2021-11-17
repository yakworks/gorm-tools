/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api


import groovy.transform.CompileStatic

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.support.MsgSourceResolvable
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.problem.Exceptional
import yakworks.api.problem.ProblemTrait
import yakworks.api.problem.RuntimeProblem
import yakworks.i18n.MsgKey

import static java.util.Arrays.asList
import static yakworks.api.problem.spi.StackTraceProcessor.COMPOUND

/**
 * an extension of the default ValidationException so you can pass the entity and the message source
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class EntityValidationProblem extends DataIntegrityViolationException implements ProblemTrait<EntityValidationProblem>, Exceptional  {
    public static String DEFAULT_CODE ='validation.problem'
    public static String DEFAULT_TITLE ='Validation Error(s)'
    Object entity //the entity that the error occured on
    Errors errors
    String exMessage

    //overrides
    String defaultCode = DEFAULT_CODE
    String title = DEFAULT_TITLE
    ApiStatus status = HttpStatus.UNPROCESSABLE_ENTITY

    EntityValidationProblem(Throwable cause) {
        this("", null, cause)
    }

    // deprecated, provided for backward compat
    EntityValidationProblem(String message) {
        this(message, null, null)
    }

    // deprecated, provided for backward compat
    EntityValidationProblem(String message, Errors e, Throwable cause) {
        super(DEFAULT_CODE, cause)
        setMsg(MsgKey.of(DEFAULT_CODE))
        setDetail(message?:cause?.message)
        errors = errors ?: new EmptyErrors("empty")
    }

    EntityValidationProblem(final EntityValidationProblem cause) {
        super(DEFAULT_CODE, cause);
        final Collection<StackTraceElement> stackTrace = COMPOUND.process(asList(getStackTrace()));
        setStackTrace(stackTrace as StackTraceElement[]);
    }

    // legacy
    EntityValidationProblem(MsgSourceResolvable msrKey, Object entity, Errors ers) {
        super(msrKey.code)
        this.entity = entity
        this.errors = ers ?: new EmptyErrors("empty")
        msg = MsgKey.of(msrKey.code, msrKey.args)
    }

    EntityValidationProblem entity(Object v) {
        if(v == null) return this
        this.entity = v
        putArgIfAbsent('name', v.class.simpleName)
        return this;
    }

    EntityValidationProblem errors(Errors v) {this.errors = v; return this;}

    @Override //throwable
    String getMessage() {
        return RuntimeProblem.buildMessage(this)
    }

    @Override
    String toString() {
        return RuntimeProblem.buildToString(this)
    }

    EntityValidationProblem notSavedMsg() {
        msg = MsgKey.of('error.persist', [entityName: entity.class.simpleName])
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
        return new EntityValidationProblem('').msg(msg)
    }

    static EntityValidationProblem of(final Throwable cause) {
        return new EntityValidationProblem(cause);
    }

    static EntityValidationProblem of(Object entity, Throwable cause) {
        return new EntityValidationProblem(cause).entity(entity);
    }
}
