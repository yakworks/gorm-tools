/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.responder

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered

import gorm.tools.mango.api.QueryArgs
import yakworks.api.problem.data.DataProblem
import yakworks.gorm.config.QueryConfig
import yakworks.security.gorm.api.UserSecurityConfig
import yakworks.security.user.CurrentUser

/**
 * Sets user specific extended value for query timeout and max, if configured for logged in user.
 */
@CompileStatic
class SecurityEntityResponderValidator implements EntityResponderValidator, Ordered {

    @Autowired QueryConfig queryConfig
    @Autowired UserSecurityConfig userSecurityConfig
    @Autowired CurrentUser currentUser

    @Override
    QueryArgs validate(QueryArgs qargs) {

        Integer timeout = getTimeout()
        if(timeout) qargs.timeout(timeout)

        Integer max = getMax()

        if(max && qargs.pager.max && qargs.pager.max > max) {
            throw DataProblem.of("error.query.max", [max:max]).toException()
        }

        qargs
    }

    int getTimeout() {
        Integer timeout = userSecurityConfig.getQueryTimeout(currentUser)
        if(!timeout) timeout = queryConfig.timeout
        return timeout
    }

    int getMax() {
        Integer max = userSecurityConfig.getMax(currentUser)
        if(!max) max = queryConfig.max
        return max
    }

    @Override
    int getOrder() {
        //higher value = lower precedence, so this will run after DefaultEntityResponderValidator
        return Ordered.HIGHEST_PRECEDENCE + 1000
    }
}
