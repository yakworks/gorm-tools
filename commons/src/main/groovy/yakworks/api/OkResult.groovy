/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api;

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor

import yakworks.i18n.MsgKey

/**
 * Simple OkResult with a Map as the data object
 *
 * @author Joshua Burnett (@basejump)
 */
// @MapConstructor
@CompileStatic
class OkResult implements ResultTrait<OkResult> {

    OkResult(){}
    OkResult(ApiStatus v){ this.status = v }
    OkResult(MsgKey mk){ this.msg = mk }

    static OkResult get() {
        return new OkResult();
    }

    static OkResult of(Integer statusCode) {
        return new OkResult(HttpStatus.valueOf(statusCode));
    }
    static OkResult of(String code, Object args) {
        return new OkResult(MsgKey.of(code, args));
    }

}
