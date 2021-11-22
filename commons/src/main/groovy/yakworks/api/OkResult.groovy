/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api

import groovy.transform.CompileStatic

import yakworks.i18n.MsgKey

/**
 * Simple OkResult with a Map as the data object
 *
 * @author Joshua Burnett (@basejump)
 */
// @MapConstructor
@CompileStatic
class OkResult implements ResultTrait<OkResult> {

    OkResult(){ }
    OkResult(Object payload){ this.payload = payload }
    OkResult(MsgKey mk){ this.msg = mk }

    static OkResult ofMsg(MsgKey mk) {
        return new OkResult(mk);
    }

}
