/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileStatic

import yakworks.security.user.BasicUserInfo
import yakworks.security.user.CurrentUser
import yakworks.security.user.UserInfo

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class CurrentTestUser implements CurrentUser {

    UserInfo user = BasicUserInfo.create(
        id:1L, username: "testuser", email: "testuser@testing.com", roles: ["ADMIN"], orgId:1L
    )

    @Override
    boolean isLoggedIn() {
        return true
    }

    @Override
    void logout() {

    }

    @Override
    boolean hasAnyRole(Collection<String> roles){
        return true
    }

    @Override
    boolean hasRole(String role) {
        return false
    }

    @Override
    boolean hasPermission(String permission) {
        return true
    }
}
