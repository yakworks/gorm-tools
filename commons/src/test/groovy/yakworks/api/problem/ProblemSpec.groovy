/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import spock.lang.Specification

import static yakworks.api.HttpStatus.NOT_FOUND

class ProblemSpec extends Specification {

    void shouldRenderTestProblem() {
        expect:
        Problem problem = Problem.create()
        problem.toString() == "{400, Bad Request}"
    }

    void shouldRenderCustomDetailAndInstance() {
        when:
        final RuntimeProblem p = Problem.create()
            .type(URI.create("https://example.org/problem"))
            .status(NOT_FOUND)
            .detail("Order 123")
            .instance(URI.create("https://example.org/"))

        then:
        p.type.toString() == "https://example.org/problem"
        // p.title == "Not Found"
        p.status == NOT_FOUND
        p.detail == "Order 123"
        p.instance as String == "https://example.org/"

    }

    void shouldRenderCustomPropertiesWhenPrintingStackTrace() {
        when:
        final RuntimeProblem problem = Problem.create()
            .type(URI.create("https://example.org/problem"))
            .status(NOT_FOUND)

        final StringWriter writer = new StringWriter()
        problem.printStackTrace(new PrintWriter(writer))

        then:
        writer.toString().startsWith("{404, Not Found")
    }

}
