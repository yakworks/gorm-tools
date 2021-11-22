/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data;

import groovy.transform.CompileStatic;

/**
 * Base interface for statuses.
 */
@CompileStatic
public interface ProblemType {

    /**
     * the code
     */
    String getCode();

}
