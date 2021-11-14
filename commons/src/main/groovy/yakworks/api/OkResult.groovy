/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api

import groovy.transform.CompileStatic

/**
 * Used as a result object in api for any api that can be ok or may have a problem
 *
 * @author Joshua Burnett (@basejump)
 */

@CompileStatic
class OkResult implements ResultTrait<OkResult, Object>, Serializable {

    Boolean ok = true

    OkResult(Integer status){
        this.status = status
    }
    // Boolean ok = true
    //
    // OkResult status(Integer v){ status = v; return this; }
    // OkResult title (String v) { title = v;  return this; }
    // OkResult data(Object v)        { data = v; return this; }
    // OkResult target(Object v) { target = v; return this; }
    //
    // static OkResult of(Integer statusId){
    //     return new OkResult(status: statusId)
    // }
}
