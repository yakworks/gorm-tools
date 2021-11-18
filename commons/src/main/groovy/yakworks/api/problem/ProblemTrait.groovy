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
 * Trait implementation for the Problem that has setters and builders
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait ProblemTrait<E extends Problem> implements Problem {
    // String defaultCode = 'error.default'
    URI type //= Problem.DEFAULT_TYPE
    ApiStatus status = HttpStatus.BAD_REQUEST
    MsgKey msg
    String title
    String detail
    Object payload
    URI instance

    Map<String, Object> parameters = Collections.emptyMap()

    List<Violation> violations = [] as List<Violation> //Collections.emptyList();

    E status(Integer v) { setStatus(HttpStatus.valueOf(v)); return (E)this; }
    E status(ApiStatus v) {
        setStatus(v)
        // if(getTitle() == null) setTitle(status.getReason())
        return (E)this
    }

    E msg(MsgKey v){ setMsg(v); return (E)this; }
    E msg(String code, Object args) { return msg(MsgKey.of(code, args));}
    E title(String v) { setTitle(v);  return (E)this; }
    E detail(String v) { setDetail(v);  return (E)this; }
    E payload(Object v) { setPayload(v); return (E)this; }

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
