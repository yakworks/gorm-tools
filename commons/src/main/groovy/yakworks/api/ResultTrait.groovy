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
trait ResultTrait implements Result {
    String defaultCode = 'result.ok'
    Boolean ok = true
    ApiStatus status = HttpStatus.OK
    MsgKey msg
    String title
    Object data

    ResultTrait msg(MsgKey v){ setMsg(v); return this; }
    ResultTrait msg(String v, Map args) { return msg(MsgKey.of(v, args));}
    ResultTrait title(String v) { setTitle(v);  return this; }
    ResultTrait status(ApiStatus v) { setStatus(v); return this; }
    ResultTrait status(Integer v) { setStatus(HttpStatus.valueOf(v)); return this; }
    ResultTrait data(Object v) { setData(v); return this; }
    // E value(T v){ setValue(v); return (E)this; }

    /**
     * adds an enrty to the msg arg map
     */
    Map putArgIfAbsent(Object key, Object val){
        if(!msg) msg = MsgKey.of(defaultCode)
        return msg.putArg(key, val)
    }
}
