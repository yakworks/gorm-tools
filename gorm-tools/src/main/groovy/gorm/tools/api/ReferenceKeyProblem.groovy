/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import javax.annotation.Nullable

import groovy.transform.CompileStatic

import yakworks.api.problem.RuntimeProblem
import yakworks.i18n.MsgKey

/**
 * Throwable Exception Problem
 */
@CompileStatic
class ReferenceKeyProblem extends RuntimeProblem {
    public static String DEFAULT_CODE = 'error.reference.key'
    String defaultCode = DEFAULT_CODE
    Object entity

    protected ReferenceKeyProblem() {
        super(null);
    }

    ReferenceKeyProblem reference(String otherName) {
        putArgIfAbsent('other', otherName)
        return this;
    }

    //pass in an entity desc instead
    static ReferenceKeyProblem of(String entityDesc) {
        def opProb = new ReferenceKeyProblem()
        opProb.msg = MsgKey.of(DEFAULT_CODE, [name: entityDesc])
        return opProb;
    }

    static ReferenceKeyProblem of(Object entity) {
        def opProb = new ReferenceKeyProblem()
        def id = entity['id']
        opProb.msg = MsgKey.of(DEFAULT_CODE, [name: entity.class.simpleName, id:id])
        return opProb;
    }

}
