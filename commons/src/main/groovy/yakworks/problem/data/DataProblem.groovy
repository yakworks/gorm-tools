/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

/**
 * generic problem
 */
@CompileStatic
class DataProblem implements DataProblemTrait<DataProblem> {
    String defaultCode = 'error.data.problem'
    /**
     * helper for legacy to throw a DataProblemException with a message
     */
    static DataProblemException ex(String message){
        return (DataProblemException) DataProblem.withTitle(message).toException()
    }
}
