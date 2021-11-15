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
trait ProblemTrait implements Problem {

    URI type //= Problem.DEFAULT_TYPE
    ApiStatus status = HttpStatus.BAD_REQUEST
    MsgKey msg
    String title
    String detail
    Object data
    URI instance

    Map<String, Object> parameters = Collections.emptyMap()

    List<Violation> errors = Collections.emptyList();

    ProblemTrait status(Integer v) { setStatus(HttpStatus.valueOf(v)); return this; }
    ProblemTrait status(ApiStatus v) {
        setStatus(v)
        if(getTitle() == null) setTitle(status.getReason())
        return this
    }

    ProblemTrait msg(MsgKey v){ setMsg(v); return this; }
    ProblemTrait msg(String code, Map args) { return msg(MsgKey.of(code, args));}
    ProblemTrait title(String v) { setTitle(v);  return this; }
    ProblemTrait detail(String v) { setDetail(v);  return this; }
    ProblemTrait data(Object v) { setData(v); return this; }

    ProblemTrait type(URI v) { setType(v); return this; }
    ProblemTrait type(String v) { setType(URI.create(v)); return this; }
    ProblemTrait instance(URI v) { setInstance(v); return this; }
    ProblemTrait instance(String v) { setInstance(URI.create(v)); return this; }

    ProblemTrait errors(List<Violation> v) { setErrors(v); return this; }

    // E value(T v){ setValue(v); return (E)this; }

    void set(String key, Object value) {
        parameters.put(key, value);
    }

    // static String genString(final Problem p) {
    //     String concat = "${p.status.code}"
    //     concat = [concat, p.title, p.detail].findAll({it != null}).join(', ')
    //     if(p.instance) concat = "$concat, instance=${p.instance}"
    //     if(p.type) concat = "$concat, type=${p.type}"
    //     return "{$concat}"
    // }
}
