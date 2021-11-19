/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data


import groovy.transform.CompileStatic

import yakworks.i18n.MsgKey

/**
 * Throwable Exception Problem
 */
@CompileStatic
class ReferenceKeyProblem extends AbstractDataAccessProblem<ReferenceKeyProblem> {
    public static String DEFAULT_CODE = 'error.reference.key'
    String defaultCode = DEFAULT_CODE

    protected ReferenceKeyProblem() {
        super(null);
    }

    ReferenceKeyProblem reference(String otherName) {
        args.putIfAbsent('other', otherName)
        return this;
    }

    //pass in an entity desc instead
    static ReferenceKeyProblem ofStamp(String entityDesc) {
        def opProb = new ReferenceKeyProblem()
        opProb.msg = MsgKey.of(DEFAULT_CODE, [stamp: entityDesc])
        return opProb;
    }

    static ReferenceKeyProblem of(Object entity) {
        def opProb = new ReferenceKeyProblem()
        return opProb.entity(entity);
    }

}
