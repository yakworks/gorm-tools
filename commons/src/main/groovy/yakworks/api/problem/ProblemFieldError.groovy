/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api.problem

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

@ToString @EqualsAndHashCode
@TupleConstructor
@CompileStatic
class ProblemFieldError implements Serializable {
    String code
    String message
    String field

    static ProblemFieldError of(String code, String message) {
        new ProblemFieldError(code, message)
    }
}
