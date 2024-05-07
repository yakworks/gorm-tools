/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import javax.inject.Inject

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.event.PreQueryEvent
import org.grails.orm.hibernate.query.AbstractHibernateQuery
import org.grails.orm.hibernate.query.HibernateHqlQuery
import org.springframework.context.ApplicationListener

import yakworks.security.user.CurrentUser

/**
 * Sets query timeout for hibernate queries.
 * Enables to set extended query timeout for specific users
 */
@CompileStatic
class GormToolsPreQueryEventListener implements ApplicationListener<PreQueryEvent> {

    @Inject CurrentUser currentUser
    @Inject QueryTimeoutConfig queryTimeoutConfig

    @Override
    @CompileDynamic
    void onApplicationEvent(PreQueryEvent event) {

        int queryTimeout = queryTimeoutConfig.query

        if(extendedQueryTimeoutEnabledForCurrentUser()) {
            queryTimeout = queryTimeoutConfig.users[currentUser.user.username].queryTimeout
        }


        //this would set query timeout on underlying jdbc statement.
        if(event.query instanceof AbstractHibernateQuery) {
            ((AbstractHibernateQuery)event.query).@criteria.setTimeout(queryTimeout)
        } else if (event.query instanceof HibernateHqlQuery) {
            ((HibernateHqlQuery)event.query).query.setTimeout(queryTimeout)
        }
    }

    boolean extendedQueryTimeoutEnabledForCurrentUser() {
        if(!queryTimeoutConfig.users || !currentUser || !currentUser.loggedIn) return false
        return (queryTimeoutConfig.users.containsKey(currentUser.user.username))
    }
}
