/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.event.PreQueryEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.Ordered

/**
 * Sets query timeout for hibernate queries.
 */
@CompileStatic
class GormToolsPreQueryEventListener extends AbstractQueryListener implements ApplicationListener<PreQueryEvent>, Ordered {

    @Inject QueryConfig queryConfig

    @Override
    void onApplicationEvent(PreQueryEvent event) {

        //If its -1, means no timeout, that's default, and dont need to be set on query.
        if (queryConfig.timeout > 0) {
            setTimeout(event.query, queryConfig.timeout)
        }

        if(queryConfig.max > 0) {
            setMax(event.query, queryConfig.max)
        }
    }

    //high precedence, so that it runs before RallyPreQueryEventListener
    @Override
    int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE
    }
}
