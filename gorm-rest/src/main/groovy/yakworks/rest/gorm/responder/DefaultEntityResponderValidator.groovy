/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.responder

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

import gorm.tools.hibernate.QueryConfig
import gorm.tools.mango.api.QueryArgs

@CompileStatic
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultEntityResponderValidator implements EntityResponderValidator {

    @Autowired QueryConfig queryConfig

    QueryArgs validate(QueryArgs qargs) {
        if (queryConfig.timeout) {
            qargs.timeout = queryConfig.timeout
        }

        //set max on qargs from query config. dont force query config max, if user supplied max is smaller thn query config
        if (queryConfig.max && qargs.pager.max && qargs.pager.max > queryConfig.max) {
            qargs.pager.max = queryConfig.max
        }

        qargs
    }
}
