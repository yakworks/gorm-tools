/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem


import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.Result
import yakworks.api.ResultTrait
import yakworks.i18n.MsgKey

/**
 * Trait implementation for the Problem that has setters and builders
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait ProblemTrait<E extends Problem> extends ResultTrait<E> implements Problem {
    // result overrides
    Boolean getOk(){ false }
    ApiStatus status = HttpStatus.BAD_REQUEST

    // Problem impls
    URI type //= Problem.DEFAULT_TYPE
    String detail
    URI instance

    List<Violation> violations = [] as List<Violation> //Collections.emptyList();

    E detail(String v) { setDetail(v);  return (E)this; }
    E type(URI v) { setType(v); return (E)this; }
    E type(String v) { setType(URI.create(v)); return (E)this; }
    E instance(URI v) { setInstance(v); return (E)this; }
    E instance(String v) { setInstance(URI.create(v)); return (E)this; }
    E violations(List<Violation> v) { setViolations(v); return (E)this; }

    E addErrors(List<MsgKey> keyedErrors){
        def ers = getViolations()
        keyedErrors.each {
            ers << ViolationFieldError.of(it)
        }
        return (E)this
    }

}
