/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileStatic

import yakworks.security.SecService
import yakworks.security.UserInfo
import yakworks.security.gorm.model.SecRole

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class TestingSecService<D extends UserInfo> implements SecService<D> {

    TestingSecService(Class<D> clazz) {
        this.entityClass = clazz
    }

    Long userId = 1

    /**
     * Gets the currently logged in user id from principal
     */
    @Override
    Long getUserId() {
        userId
    }

    /**
     * Encode the password using the configured PasswordEncoder.
     * calls same method on springSecurityService
     */
    @Override
    String encodePassword(String password) {
        password
    }

    /**
     * Quick check to see if the current user is logged in.
     * calls same method on springSecurityService
     * @return <code>true</code> if the authenticated and not anonymous
     */
    @Override
    boolean isLoggedIn() {
        return true
    }

    /**
     * Check if current user has any of the specified roles
     */
    @Override
    boolean ifAnyGranted(String... roles) {
        return true
    }

    /**
     * Check if current user has all of the specified roles
     */
    @Override
    boolean ifAllGranted(String... roles) {
        return true
    }


    @Override
    void loginAsSystemUser() {

    }

    @Override
    void logout() {

    }

    @Override
    void reauthenticate(String username, String password = null) {

    }
    /**
     * Get the current user's roles.
     * @return a list of roles (empty if not authenticated).
     */
    Set<String> getPrincipalRoles() {
        if (!isLoggedIn()) return new HashSet<>()
        return user.roles as Set<String>
    }



}
