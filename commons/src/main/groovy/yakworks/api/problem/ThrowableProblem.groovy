/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import javax.annotation.Nullable

import groovy.transform.CompileStatic

import static java.util.Arrays.asList
import static yakworks.api.problem.spi.StackTraceProcessor.COMPOUND

/**
 * Throwable Exception Problem
 */
@CompileStatic
class ThrowableProblem extends RuntimeException implements ProblemTrait, Exceptional {

    protected ThrowableProblem() {
        this(null);
    }

    protected ThrowableProblem(@Nullable final ThrowableProblem cause) {
        super(cause);
        final Collection<StackTraceElement> stackTrace = COMPOUND.process(asList(getStackTrace()));
        setStackTrace(stackTrace as StackTraceElement[]);
    }

    @Override //throwable
    String getMessage() {
        return title + (detail? ": $detail" : '')
    }

    @Override
    ThrowableProblem getCause() {
        // cast is safe, since the only way to set this is our constructor
        return (ThrowableProblem) super.getCause()
    }

    @Override
    String toString() {
        return genString(this)
    }

    static String genString(final Problem p) {
        String concat = "${p.status.code}"
        concat = [concat, p.title, p.detail].findAll{it != null}.join(', ')
        if(p.instance) concat = "$concat, instance=${p.instance}"
        if(p.type) concat = "$concat, type=${p.type}"
        return "{$concat}"
    }

    static ThrowableProblem of(final ThrowableProblem cause) {
        return new ThrowableProblem(cause);
    }

}
