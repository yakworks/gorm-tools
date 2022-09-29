/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.springframework.core.GenericTypeResolver

import yakworks.security.gorm.model.AppUser

/**
 * common generic helpers for security, implement with generics D for the domain entity and I for the id type
 */
@CompileStatic
trait CurrentUser {

    /**
     * gets the current user info
     */
    abstract UserInfo getUserInfo()

    /**
     * is a user logged in
     */
    abstract boolean isLoggedIn()

    abstract void logout()

    abstract boolean ifAnyGranted(String... roles)

    /**
     * Check if current user has all of the specified roles
     */
    abstract boolean ifAllGranted(String... roles)

    /**
     * gets the user id from username, hard wired to AppUser.getByUsername.
     * Used when User is something like OauthUser
     */
    Serializable getUserIdByUsername(String username) {
        if (!isLoggedIn()) {
            return null
        }
        return AppUser.getByUsername(username.trim()).id
    }

    /**
     * returns the name property from the AppUser for logged in user
     * @return the username
     */
    String getUserFullName() {
        return getUserFullName(getUserId())
    }

    /**
     * returns the name
     * @return the full name / display name
     */
    String getUserFullName(Serializable uid) {
        D usr = getUser(uid)
        return usr ? usr['name'] : null
    }

    /**
     * returns the username handle for logged in user
     * @return the username
     */
    String getUsername() {
        return getUsername(getUserId())
    }

    /**
     * returns the username handle for the passed in id
     * @return the username
     */
    String getUsername(Serializable uid) {
        D usr = getUser(uid)
        return usr ? usr['username'] : null
    }

    /**
     * returns first section of email before @
     * @return the username
     */
    // String getDisplayName(Serializable uid) {
    //     D usr = getUser(uid)
    //     if(!usr) return null
    //     String email = usr['email']
    //     return email.substring(0, email.indexOf("@"))
    // }

    /**
     * get the user entity for the id
     * @param uid the user id
     * @return the user entity
     */
    D getUser(Serializable uid) {
        GormEnhancer.findStaticApi(getEntityClass()).get(uid)
    }

}
