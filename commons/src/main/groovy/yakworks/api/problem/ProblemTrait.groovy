/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.i18n.MsgKey

/**
 * Trait implementation for the Problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait ProblemTrait<E> implements ProblemBase<E> {

    URI type = ProblemBase.DEFAULT_TYPE
    ApiStatus status = HttpStatus.BAD_REQUEST
    MsgKey msg
    String title
    String detail
    Object value
    Object data

    List<Violation> violations = Collections.emptyList();
}
