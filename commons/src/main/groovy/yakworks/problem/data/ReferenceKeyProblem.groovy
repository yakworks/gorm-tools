/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data


import groovy.transform.CompileStatic

import yakworks.i18n.MsgKey
import yakworks.problem.exception.NestedProblemException

/**
 * Throwable Exception Problem
 */
@CompileStatic
class ReferenceKeyProblem extends NestedProblemException implements DataProblemTrait<ReferenceKeyProblem>  {
    String defaultCode = 'error.reference.key'

    ReferenceKeyProblem() {
        super();
    }

    ReferenceKeyProblem reference(String otherName) {
        args.putIfAbsent('other', otherName)
        return this;
    }

    //pass in an entity desc instead
    static ReferenceKeyProblem withStamp(String entityDesc) {
        def opProb = new ReferenceKeyProblem()
        opProb.msg = MsgKey.of(opProb.defaultCode, [stamp: entityDesc])
        return opProb;
    }

}
