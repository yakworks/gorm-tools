/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api


import groovy.transform.CompileStatic

import yakworks.i18n.MsgKey

/**
 * Throwable Exception Problem
 */
@CompileStatic
class OptimisticLockingProblem extends DataAccessProblem<OptimisticLockingProblem> {
    public static String DEFAULT_CODE = 'error.optimisticLocking'
    String defaultCode = DEFAULT_CODE
    Object entity

    protected OptimisticLockingProblem() {
        super(null);
    }

    @Override
    OptimisticLockingProblem getCause() {
        // cast is safe, since the only way to set this is our constructor
        return (OptimisticLockingProblem) super.getCause()
    }

    OptimisticLockingProblem entity(Object v) {
        if(v == null) return this
        this.entity = v
        putArgIfAbsent('name', v.class.simpleName)
        return this;
    }

    static OptimisticLockingProblem of(Object entity) {
        def opProb = new OptimisticLockingProblem()
        opProb.msg = MsgKey.of(DEFAULT_CODE, [name: entity.class.simpleName])
        return opProb;
    }

}
