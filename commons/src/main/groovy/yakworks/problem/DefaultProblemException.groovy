/*
* Copyright 2002-2018 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem


import groovy.transform.CompileStatic

/**
 * Handy class for wrapping runtime {@code Exceptions} with a root cause.
 *
 * <p>This class is {@code abstract} to force the programmer to extend
 * the class. {@code printStackTrace} and other like methods will
 * delegate to the wrapped exception, if any.
 *
 */
@CompileStatic
class DefaultProblemException extends ProblemException<ProblemTrait> {

    DefaultProblemException() {
        super();
    }

    DefaultProblemException(String msg) {
        super(msg);
    }

    DefaultProblemException(Throwable cause) {
        super(cause);
    }

}
