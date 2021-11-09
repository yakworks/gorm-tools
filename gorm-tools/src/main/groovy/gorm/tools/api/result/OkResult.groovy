/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api.result

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.support.MsgSourceResolvable

/**
 * Used as a result object in api for any api that can be ok or may have a problem
 *
 * @author Joshua Burnett (@basejump)
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@ToString @EqualsAndHashCode
@CompileStatic
class OkResult implements Result, MsgSourceResolvable, Serializable {
    //reimplement Result fields so @Builder will see them, easier to do this than to manually implement builder methods
    Integer status; String title; Object data;

    final Boolean ok = true

    static OkResult of(String code, List args = null){
        def prob = new OkResult()
        prob.setMessage(code, args)
        return prob
    }

    static OkResult of(Object target){
        new OkResult(target: target)
    }

    static OkResult of(Object target, Integer statusId){
        of(target).status(statusId)
    }

}
