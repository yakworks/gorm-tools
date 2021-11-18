/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import javax.annotation.Nullable

import groovy.transform.CompileStatic

import yakworks.commons.lang.NestedRuntimeException

import static java.util.Arrays.asList
import static yakworks.api.problem.spi.StackTraceProcessor.COMPOUND

/**
 * Throwable Exception Problem
 */
@CompileStatic
class ProblemException extends NestedRuntimeException implements ProblemTrait<ProblemException>, Exceptional {

    protected ProblemException() {
        this(null);
    }

    protected ProblemException(@Nullable final ProblemException cause) {
        super(cause);
        final Collection<StackTraceElement> stackTrace = COMPOUND.process(asList(getStackTrace()));
        setStackTrace(stackTrace as StackTraceElement[]);
    }

    @Override //throwable
    String getMessage() {
        return ProblemException.buildMessage(this)
    }

    static String buildMessage(final Problem p) {
        String code = p.code ? "code=$p.code" : ''
        return [p.title, p.detail, code].findAll{it}.join(': ')
    }

    @Override
    ProblemException getCause() {
        // cast is safe, since the only way to set this is our constructor
        return (ProblemException) super.getCause()
    }

    @Override
    String toString() {
        return ProblemException.buildToString(this)
    }

    static String buildToString(final Problem p) {
        String concat = "${p.status.code}"
        String title = p.title ?: p.status.reason
        concat = [concat, title, p.detail].findAll{it != null}.join(', ')
        if(p.instance) concat = "$concat, instance=${p.instance}"
        if(p.type) concat = "$concat, type=${p.type}"
        return "{$concat}"
    }

    static ProblemException of(final ProblemException cause) {
        return new ProblemException(cause);
    }

}
