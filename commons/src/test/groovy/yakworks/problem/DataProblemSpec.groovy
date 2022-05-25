/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import spock.lang.Specification
import yakworks.problem.data.DataProblem
import yakworks.problem.data.DataProblemException
import yakworks.problem.data.DataProblemCodes

class DataProblemSpec extends Specification {

    void "DataProblemKinds sanity check"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def e = DataProblemCodes.ReferenceKey.get().cause(rte).toException()

        then:
        e.code == 'error.data.reference'
        e.rootCause == rte
    }

    void "DataProblem entity payload"() {
        when:
        def someEntity = new SomeEntity()
        def e = DataProblem.of(someEntity).msg('foo').toException()

        then:
        e instanceof DataProblemException
        e instanceof ProblemException
        e.problem
        e.code == 'foo'
        e.args.asMap().name == 'SomeEntity'
    }

    void "DataProblem ex exception"() {
        when:
        DataProblemException e = DataProblem.ex("foo error")

        then:
        e.detail == "foo error"
        e.message.startsWith "foo error"
        e instanceof ProblemException
        e.problem instanceof DataProblem
    }

    static class SomeEntity {

    }
}
