/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.listeners

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.event.PreQueryEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.Ordered

import gorm.tools.hibernate.AbstractQueryListener
import yakworks.rally.security.UserSecurityConfig
import yakworks.security.user.CurrentUser

/**
 * Sets query timeout for hibernate queries.
 * Enables to set extended query timeout for specific users
 */
@CompileStatic
//FIXME remove
class RallyPreQueryEventListener extends AbstractQueryListener implements ApplicationListener<PreQueryEvent>, Ordered {

    @Inject CurrentUser currentUser
    @Inject UserSecurityConfig userSecurityConfig

    @Override
    void onApplicationEvent(PreQueryEvent event) {
        if (!currentUser || !currentUser.loggedIn) return

        UserSecurityConfig.UserConfig userQueryConfig = getUserConfig(currentUser.user.username)
        if (userQueryConfig) {
            if (userQueryConfig.queryTimeout > 0) {
                setTimeout(event.query, userQueryConfig.queryTimeout)
            }

            /* XXX @Josh setMax in AbstractQueryListener is commented?
            if (userQueryConfig.queryMax > 0) {
                setMax(event.query, userQueryConfig.queryMax)
            }*/
        }
    }

    UserSecurityConfig.UserConfig getUserConfig(String username) {
        if (userSecurityConfig.users) {
            return userSecurityConfig.users[username]
        } else {
            return null
        }
    }

    //low precedence, so that it runs after GormToolsQueryListener and can override user specific values
    @Override
    int getOrder() {
        return Ordered.LOWEST_PRECEDENCE
    }
}
