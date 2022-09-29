/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileStatic

import yakworks.security.SecService
import yakworks.security.gorm.model.AppUser

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class TestingSecService implements SecService<AppUser> {

    TestingSecService() {
        this.entityClass = AppUser
    }

    /**
     * Encode the password using the configured PasswordEncoder.
     * calls same method on springSecurityService
     */
    @Override
    String encodePassword(String password) {
        password
    }

    @Override
    void loginAsSystemUser() {

    }

    @Override
    void reauthenticate(String username, String password = null) {

    }

}
