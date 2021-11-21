/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

import yakworks.problem.DefaultProblemException
import yakworks.problem.ProblemException

/**
 * generic problem
 */
@CompileStatic
class DataProblemException extends ProblemException<DataProblemTrait<?>> {

    // @Delegate DataProblem dataProblem
    //
    // IProblem getProblem(){ dataProblem }
    // void setProblem(IProblem v){ setDataProblem((DataProblem)v) }

    DataProblemException() {super()}
    DataProblemException(Throwable cause) {super(cause)}

}
