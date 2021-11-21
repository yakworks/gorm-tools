/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import spock.lang.Specification
import spock.lang.Unroll
import yakworks.i18n.MsgKey
import yakworks.problem.data.OptimisticLockingProblem
import yakworks.problem.exception.ThrowableProblem

import static yakworks.api.HttpStatus.NOT_FOUND

class DataProblemSpec extends Specification {

    void "OptimisticLockingProblem cause"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def e = OptimisticLockingProblem.cause(rte)

        then:
        e.code == 'error.optimisticLocking'
        e.rootCause == rte
    }

    void "OptimisticLockingProblem entity payload"() {
        when:
        def someEntity = new SomeEntity()
        def e = OptimisticLockingProblem.of(someEntity)

        then:
        e.code == 'error.optimisticLocking'
        e.args.asMap().name == 'SomeEntity'
    }

    static class SomeEntity {

    }
}
