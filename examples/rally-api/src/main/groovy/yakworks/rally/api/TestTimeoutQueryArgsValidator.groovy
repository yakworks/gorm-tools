/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import org.hibernate.QueryTimeoutException

import gorm.tools.mango.api.QueryArgs
import yakworks.security.gorm.api.UserQueryArgsValidator

/*
 * Overrides to test the query timeout
 */
@CompileStatic
class TestTimeoutQueryArgsValidator extends UserQueryArgsValidator {

    @Override
    QueryArgs validate(QueryArgs qargs) {
        //throw timeout if param is present in qarg
        if(qargs.criteriaMap['timeout']) {
            throw new QueryTimeoutException("Test query timeout", null, null)
        }
        super.validate(qargs)
    }
}
