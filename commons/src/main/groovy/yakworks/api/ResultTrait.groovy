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
trait ResultTrait<E> implements Result {
    String defaultCode = 'result.ok'
    Boolean ok = true
    ApiStatus status = HttpStatus.OK
    MsgKey msg
    String title
    Object payload

    E msg(MsgKey v){ setMsg(v); return (E)this; }
    E msg(String v, Object args) { return msg(MsgKey.of(v, args));}
    E title(String v) { setTitle(v);  return (E)this; }
    E status(ApiStatus v) { setStatus(v); return (E)this; }
    E status(Integer v) { setStatus(HttpStatus.valueOf(v)); return (E)this; }
    E payload(Object v) { setPayload(v); return (E)this; }
    // E value(T v){ setValue(v); return (E)this; }

    /**
     * adds an enrty to the msg arg map
     */
    Map putArgIfAbsent(Object key, Object val){
        if(!msg) msg = MsgKey.of(defaultCode)
        return msg.putArg(key, val)
    }
}
