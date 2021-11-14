/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import groovy.transform.CompileStatic

import yakworks.api.ResultTrait

/**
 * Trait implementation for the Problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait ProblemTrait<E> extends ResultTrait<E, Object> implements Problem {
    Boolean ok = false
    Integer status = 400
    String detail

    ProblemTrait detail(String v) { detail = v; return this }

}
