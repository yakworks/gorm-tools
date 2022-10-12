/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user

import groovy.transform.CompileStatic

import yakworks.util.StringUtils

/**
 * CurrentUser contract to be implemented with a prefered security framework.
 * this is intentionally very basic.
 */
@CompileStatic
trait CurrentUser {

    // @Autowired MetaMapService metaMapService

    /**
     * gets the current user ID, usually a long
     */
    Serializable getUserId(){
        getUser().id
    }

    /**
     * gets the current user info
     */
    abstract UserInfo getUser()

    /**
     * is a user logged in
     */
    abstract boolean isLoggedIn()

    /**
     * logout current user
     */
    abstract void logout()

    /**
     * Returns true if this Subject has the specified role
     */
    abstract boolean hasRole(String role)

    /**
     * Returns true if current user has any of the specified roles
     */
    abstract boolean hasAnyRole(Collection<String> roles)

    /**
     * Returns true if current user has any of roles in comma seperated list
     */
    boolean hasAnyRole(String rolesString){
        hasAnyRole(StringUtils.commaDelimitedListToSet(rolesString))
    }

    /**
     * Gets user fields to send to client about their login
     */
    Map getUserMap() {
        //Will come from a customizable list. props probably
        List incs = ['id', 'username', 'name', 'email', 'orgId']
        return getUserMap(incs)
    }

    /**
     * Gets user fields to send to client about their login
     */
    Map getUserMap(List incs) {
        Map userMap = getUser().properties.subMap(incs)
        // Map userMap = metaMapService.createMetaMap(getUser(), incs).clone() as Map
        // if (isCustomer()) userMap.put('isCustomer', true)
        return userMap
    }
}
