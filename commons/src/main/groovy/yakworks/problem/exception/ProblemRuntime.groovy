/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.exception

import groovy.transform.CompileStatic

import jakarta.annotation.Nullable
import yakworks.problem.ProblemTrait
import yakworks.problem.ProblemUtils

import static java.util.Arrays.asList
import static yakworks.problem.spi.StackTraceProcessor.COMPOUND

/**
 * Throwable Exception Problem
 */
@CompileStatic
class ProblemRuntime extends NestedProblemException implements ProblemTrait<ProblemRuntime>, Exceptional {

    ProblemRuntime() {
        this(null);
    }

    ProblemRuntime(@Nullable final ProblemRuntime cause) {
        super(cause);
        final Collection<StackTraceElement> stackTrace = COMPOUND.process(asList(getStackTrace()));
        setStackTrace(stackTrace as StackTraceElement[]);
    }

    @Override //throwable
    String getMessage() {
        return ProblemUtils.buildMessage(this)
    }

    @Override
    ProblemRuntime getCause() {
        // cast is safe, since the only way to set this is our constructor
        return (ProblemRuntime) super.getCause()
    }

    static ProblemRuntime cause(final ProblemRuntime cause) {
        return new ProblemRuntime(cause);
    }

}
