/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver

import gorm.tools.metamap.services.MetaMapService
import yakworks.security.gorm.model.AppUser

/**
 * CurrentUser contract to be implemented by prefered security framework.
 */
@CompileStatic
trait CurrentUser {

    @Autowired MetaMapService metaMapService

    /**
     * gets the current user info
     */
    Serializable getUserId(){
        getUserInfo().id
    }

    /**
     * gets the current user info
     */
    abstract UserInfo getUserInfo()

    /**
     * is a user logged in
     */
    abstract boolean isLoggedIn()

    /**
     * logout current user
     */
    abstract void logout()

    /**
     * Check if current user has any of the specified roles
     */
    abstract boolean hasAnyRole(String... roles)

    abstract boolean hasRole(String role)

    /**
     * Gets user fields to send to client about their login
     */
    Map getUserMap() {
        List incs = ['id', 'username', 'name', 'email', 'orgId']
        return getUserMap(incs)
    }

    /**
     * Gets user fields to send to client about their login
     */
    Map getUserMap(List incs) {
        Map userMap = metaMapService.createMetaMap(getUserInfo(), incs).clone() as Map
        // if (isCustomer()) userMap.put('isCustomer', true)
        return userMap
    }
}
