/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import groovy.transform.CompileStatic

/**
 * Default problem
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class Problem implements ProblemTrait<Problem> {

    // allows to do 'someProblem as Exception'
    // @Override
    public <T> T asType(Class<T> clazz) {
        Exception.isAssignableFrom(clazz) ? (T) toException() : super.asType(clazz)
    }
}
