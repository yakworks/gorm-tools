/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.support


import groovy.transform.CompileStatic

import gorm.tools.mango.api.QueryArgs

/**
 * Validate the query args for things like max and timeout
 */
@CompileStatic
interface QueryArgsValidator {

    /**
     * Validates and sets up defaults
     */
    QueryArgs validate(QueryArgs qargs)

    // Serializable validateTimeout()
    //
    // void validateMax(Integer max)

}
