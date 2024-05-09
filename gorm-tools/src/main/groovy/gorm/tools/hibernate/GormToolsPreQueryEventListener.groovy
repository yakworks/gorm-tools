/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import java.lang.reflect.Field
import javax.inject.Inject

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.event.PreQueryEvent
import org.grails.orm.hibernate.query.AbstractHibernateQuery
import org.grails.orm.hibernate.query.HibernateHqlQuery
import org.hibernate.Criteria
import org.hibernate.query.Query
import org.springframework.context.ApplicationListener

import yakworks.commons.lang.Validate
import yakworks.security.user.CurrentUser
import yakworks.util.ReflectionUtils

/**
 * Sets query timeout for hibernate queries.
 * Enables to set extended query timeout for specific users
 */
@CompileStatic
class GormToolsPreQueryEventListener implements ApplicationListener<PreQueryEvent> {

    @Inject CurrentUser currentUser
    @Inject QueryTimeoutConfig queryTimeoutConfig
    @Inject UserQueryTimeoutConfig userQueryTimeoutConfig

    @Override
    @CompileDynamic
    void onApplicationEvent(PreQueryEvent event) {

        Integer queryTimeout = queryTimeoutConfig.query

        if(extendedQueryTimeoutEnabledForCurrentUser()) {
            queryTimeout = userQueryTimeoutConfig.users[currentUser.user.username].queryTimeout
        }

        if(queryTimeout <= 0) return

        //this would set query timeout on underlying hibernate criteria or hibernate query
        if(event.query instanceof AbstractHibernateQuery) {
            Criteria criteria = getPrivateFieldValue(AbstractHibernateQuery, "criteria", event.query)
            criteria.setTimeout(queryTimeout)
        } else if (event.query instanceof HibernateHqlQuery) {
            Query query = getPrivateFieldValue(HibernateHqlQuery, "query", event.query)
            query.setTimeout(queryTimeout)
        }
    }


    /**
     * Gets the value of private fields - AbstractHibernateQuery.criteria or HibernateHqlQuery.query
     */
    def getPrivateFieldValue(Class aClass, String fieldName, def object) {
        Field field = ReflectionUtils.findField(aClass, fieldName)
        Validate.notNull(field)
        ReflectionUtils.makeAccessible(field)
        def fieldValue =  field.get(object)
        Validate.notNull(fieldValue)
        return fieldValue
    }

    boolean extendedQueryTimeoutEnabledForCurrentUser() {
        if(!userQueryTimeoutConfig.users || !currentUser || !currentUser.loggedIn) return false
        return (userQueryTimeoutConfig.users.containsKey(currentUser.user.username))
    }
}
