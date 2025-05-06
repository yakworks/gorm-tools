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
 * common generic helpers trait for security.
 * Implemented in spring-kit with shiro and spring-security.
 */
@CompileStatic
trait SecService {

    Class<?> entityClass

    @Inject CurrentUser currentUser

    /**
     * Used in automation with a bot or system user, also usefull for tests
     */
    abstract UserInfo loginAsSystemUser()

    /**
     * programmatic login, skips password check and filter chain and registers the user in the context
     * @param userInfo the UserInfo object to login with
     */
    abstract UserInfo login(String username, String password = null)

    abstract UserInfo authenticate(UserInfo userInfo)

    /**
     * is a user logged in and not anonymous
     */
    abstract boolean isLoggedIn()

    /**
     * get the user entity for the user id. Default impl is to pull from DB.
     * @param uid the user id
     * @return the user entity
     */
    abstract UserInfo getUser(Serializable uid)

}
