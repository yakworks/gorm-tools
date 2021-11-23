/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem


import spock.lang.Specification
import spock.lang.Unroll
import yakworks.i18n.MsgKey

import static yakworks.api.HttpStatus.NOT_FOUND

class ProblemSpec extends Specification {

    void shouldRenderTestProblem() {
        expect:
        Problem problem = Problem.create()
        problem.toString() == "Problem(400)"
        !problem.ok
    }

    void "problem of"() {
        when:
        def p = Problem.of('error.data.empty', [name: 'foo'])

        then:
        !p.ok
        p.toString()
        p.code == 'error.data.empty'
        p.args.asMap().name == 'foo'
    }

    void "problem throw"() {
        when:
        def p = Problem.of('error.data.empty', [name: 'foo'])
        throw p.toException()

        then:
        def ex = thrown(ProblemException)
        ex.problem == p

        when:
        throw p as Exception

        then:
        def ex2 = thrown(ProblemException)
        ex2.problem == p
    }

    void "problem throw with cause"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def p = Problem.of('error.data.empty', [name: 'foo']).cause(rte)
        throw p as Exception

        then:
        def ex = thrown(ProblemException)
        ex.problem == p
        ex.rootCause == rte

    }

    @Unroll
    void "init with code statics #code"(Problem problem, String code) {
        expect:
        problem instanceof Problem
        problem.code == code

        where:
        problem                               | code
        Problem.of('code.args', [name: 'foo'])      | 'code.args'
        Problem.ofCode('ofCode')  | 'ofCode'
        Problem.ofMsg(MsgKey.ofCode('withMsg')) | 'withMsg'

    }

    void "should Render Custom Detail And Instance"() {
        when:
        final Problem p = Problem.withStatus(NOT_FOUND)
            .type(URI.create("https://example.org/problem"))
            .detail("Order 123")

        then:
        p.type.toString() == "https://example.org/problem"
        // p.title == "Not Found"
        p.status == NOT_FOUND
        p.detail == "Order 123"

    }

    void shouldRenderCustomPropertiesWhenPrintingStackTrace() {
        when:
        final Problem problem = Problem.withStatus(NOT_FOUND)
            .type(URI.create("https://example.org/problem"));


        final StringWriter writer = new StringWriter()
        problem.toException().printStackTrace(new PrintWriter(writer))

        then:
        writer.toString()
        writer.toString().startsWith("Problem(404")
    }

}
