/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem


import groovy.transform.CompileStatic

import yakworks.api.HttpStatus
import yakworks.i18n.MsgKey

/**
 * This is for the statics in the Problem interface.
 * becuase its a java interface and then we use groovy traits the compilers gets all sorts of
 * confused and needs to delegate the problem creation to here
 */
@CompileStatic
class CreateProblem {

    static Class<? extends ProblemTrait> problemClass = Problem

    static ProblemTrait create() {
        return problemClass.newInstance()
    }

    static ProblemTrait code(String code, Object args) {
        return create().msg(MsgKey.of(code, args))
    }

    static ProblemTrait code(String code) {
        return create().msg(MsgKey.ofCode(code))
    }

    static ProblemTrait status(HttpStatus status) {
        return create().status(status)
    }

    static ProblemTrait msg(MsgKey mkey) {
        return create().msg(mkey)
    }

    static ProblemTrait detail(String detail) {
        return create().detail(detail);
    }

    static ProblemTrait of(Object value) {
        return create().payload(value);
    }

}
