/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer

import yakworks.security.SecService
import yakworks.security.gorm.model.AppUser
import yakworks.security.user.UserInfo

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class TestingSecService implements SecService {

    TestingSecService() {
        this.entityClass = AppUser
    }

    UserInfo getUser(Serializable uid) {
        GormEnhancer.findStaticApi(getEntityClass()).get(uid) as UserInfo
    }

    @Override
    boolean isLoggedIn() {
        true
    }

    @Override
    UserInfo loginAsSystemUser() {

    }

    @Override
    UserInfo login(String username, String password) {

    }

    @Override
    UserInfo authenticate(UserInfo userInfo) {

    }


}
