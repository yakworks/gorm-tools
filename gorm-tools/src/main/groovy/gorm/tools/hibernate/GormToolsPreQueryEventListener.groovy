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
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener

import yakworks.security.user.CurrentUser

/**
 * Sets query timeout for hibernate queries.
 * Enables to set extended query timeout for specific users
 */
@CompileStatic
class GormToolsPreQueryEventListener implements ApplicationListener<PreQueryEvent> {

    @Inject CurrentUser currentUser

    @Value('${yakworks.gorm.hibernate.user-query-timeout.value:-1}')
    Integer extendedQueryTimeout

    @Value('${yakworks.gorm.hibernate.user-query-timeout.users}')
    List<String> users


    @Override
    @CompileDynamic
    void onApplicationEvent(PreQueryEvent event) {
        if(!extendedQueryTimeoutEnabledForCurrentUser()) return

        //this would set query timeout on underlying jdbc statement.
        if(event.query instanceof AbstractHibernateQuery) {
            ((AbstractHibernateQuery)event.query).@criteria.setTimeout(extendedQueryTimeout)
        } else if (event.query instanceof HibernateHqlQuery) {
            ((HibernateHqlQuery)event.query).query.setTimeout(extendedQueryTimeout)
        }
    }

    boolean extendedQueryTimeoutEnabledForCurrentUser() {
        if(!users || !currentUser || !currentUser.loggedIn) return false
        return (extendedQueryTimeout != -1 && users.contains(currentUser.user.username))
    }
}
