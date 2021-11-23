/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api

import groovy.transform.CompileStatic

import yakworks.i18n.MsgKey

/**
 * Groovy Trait impl for Result, provides the simple builder methods
 *
 * @author Joshua Burnett (@basejump)
 * @since 1
 */
@CompileStatic
trait ResultTrait<E extends Result.Fluent> implements Result.Fluent<E> {
    String defaultCode //= 'result.ok'
    Boolean ok = true
    ApiStatus status = HttpStatus.OK
    MsgKey msgKey
    String title
    Object payload

    MsgKey getMsg() {
        if(msgKey == null) msgKey = MsgKey.ofCode(getDefaultCode())
        return msgKey
    }
    void setMsg(MsgKey v) { msgKey = v }

    @Override
    String toString() {
        return ResultUtils.resultToString(this)
    }

}
