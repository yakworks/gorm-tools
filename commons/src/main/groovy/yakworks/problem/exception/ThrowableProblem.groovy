/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.exception

import javax.annotation.Nullable

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.ResultTrait
import yakworks.problem.ProblemTrait
import yakworks.problem.ProblemUtils

/**
 * Throwable Exception Problem
 */
@CompileStatic
class ThrowableProblem extends NestedProblemException implements ResultTrait<ThrowableProblem>, ProblemTrait<ThrowableProblem>, Exceptional {

    ThrowableProblem() {
        this(null);
    }

    ThrowableProblem(@Nullable final Throwable cause) {
        super(cause);
    }

    static ThrowableProblem create() {
        return new ThrowableProblem();
    }

    @Override //throwable
    String getMessage() {
        return ProblemUtils.buildMessage(this)
    }

    // BUILDER HELPERS

    static ThrowableProblem cause(final Throwable cause) {
        return new ThrowableProblem(cause);
    }

    static ThrowableProblem of(ApiStatus status){
        return new ThrowableProblem(status: status)
    }

}
