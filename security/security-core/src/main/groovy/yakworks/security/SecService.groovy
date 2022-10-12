/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import javax.inject.Inject

import groovy.transform.CompileStatic

import jakarta.annotation.Nullable
import yakworks.security.user.CurrentUser
import yakworks.security.user.UserInfo

/**
 * common generic helpers for security, implement with generics D for the domain entity and I for the id type
 */
@CompileStatic
trait SecService {

    Class<?> entityClass

    @Inject @Nullable
    CurrentUser currentUser

    /**
     * Used in automation to username a bot/system user, also used for tests
     * Only does it if isLog
     */
    abstract UserInfo loginAsSystemUser()

    /**
     * programmatic login, skips password check and filter chain and registers the user in the context
     * @param userInfo the UserInfo object to login with
     */
    abstract UserInfo login(String username, String password = null)

    abstract UserInfo authenticate(UserInfo userInfo)

    /**
     * is a user logged in
     */
    abstract boolean isLoggedIn()

    /**
     * get the user entity for the id. Default impl is to pull from DB.
     * @param uid the user id
     * @return the user entity
     */
    abstract UserInfo getUser(Serializable uid)

}
