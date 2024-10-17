/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.support

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.mango.api.QueryArgs
import yakworks.api.problem.data.DataProblem
import yakworks.gorm.config.QueryConfig

/**
 * Validate the query args for the max and timeout settings
 */
@CompileStatic
class DefaultQueryArgsValidator implements QueryArgsValidator {

    @Autowired QueryConfig queryConfig

    @Override
    QueryArgs validate(QueryArgs qargs) {
        qargs.timeout(getTimeout())

        Integer maxItems = getMax()
        if(maxItems && qargs.pager.max && qargs.pager.max > maxItems) {
            throw DataProblem.of("error.query.max", [max:max]).toException()
        }

        qargs
    }

    int getTimeout() {
        return queryConfig.timeout
    }

    int getMax() {
        return queryConfig.max
    }
}
