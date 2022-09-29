/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileStatic

import yakworks.security.SecService
import yakworks.security.user.BasicUserInfo
import yakworks.security.user.CurrentUser
import yakworks.security.user.UserInfo

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class CurrentTestUser implements CurrentUser{

    Long userId = 1

    /**
     * Gets the currently logged in user id from principal
     */
    @Override
    Long getUserId() {
        userId
    }

    /**
     * Quick check to see if the current user is logged in.
     * calls same method on springSecurityService
     * @return <code>true</code> if the authenticated and not anonymous
     */
    @Override
    UserInfo getUserInfo() {
        return BasicUserInfo.create(username: "testuser", email: "testuser@testing.com",roles: ["ADMIN"])
    }

    @Override
    boolean isLoggedIn() {
        return true
    }

    @Override
    void logout() {

    }

    @Override
    boolean hasAnyRole(String... roles) {
        return false
    }

    @Override
    boolean hasRole(String role) {
        return false
    }
}
