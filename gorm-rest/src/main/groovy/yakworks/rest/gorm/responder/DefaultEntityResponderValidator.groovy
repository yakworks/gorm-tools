/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.responder

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered

import gorm.tools.mango.api.QueryArgs
import yakworks.api.problem.data.DataProblem
import yakworks.gorm.config.QueryConfig
import yakworks.security.user.CurrentUser

@CompileStatic
class DefaultEntityResponderValidator implements EntityResponderValidator, Ordered {

    @Autowired QueryConfig queryConfig
    @Autowired CurrentUser currentUser

    QueryArgs validate(QueryArgs qargs) {
        if(currentUser && currentUser.loggedIn) return

        if (queryConfig.timeout) {
            qargs.timeout = queryConfig.timeout
        }

        //set max on qargs from query config. dont force query config max, if user supplied max is smaller thn query config
        if (queryConfig.max && qargs.pager.max && qargs.pager.max > queryConfig.max) {
            throw DataProblem.of("error.query.max", [max:queryConfig.max]).toException()
        }

        qargs
    }

    @Override
    int getOrder() {
        //should run before SecurityEntityResponderValidator
        return Ordered.HIGHEST_PRECEDENCE
    }
}
