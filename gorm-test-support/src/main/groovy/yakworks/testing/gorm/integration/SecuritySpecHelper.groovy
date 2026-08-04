/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.integration

import groovy.transform.CompileDynamic

import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.transaction.WithTrx
import yakworks.security.SecService
import yakworks.security.gorm.model.AppUser
import yakworks.security.spring.user.SpringUser
import yakworks.security.user.CurrentUser

/**
 * Integration support for the bootsecurity plugin.
 *
 */
@CompileDynamic
trait SecuritySpecHelper implements WithTrx{

    @Autowired @Qualifier("secService")
    SecService secService

    @Autowired CurrentUser currentUser

    //need to name it like this, otherwise subclasses cant use setupSpec method
    @Before
    void setupSecuritySpec() {
        withTrx {
            secService.loginAsSystemUser()
        }
    }

    void authenticate(AppUser user, Collection<String> roles = [], Collection<String> permissions = []) {
        def rolesToUse = user.roles
        def permsToUse = user.permissions

        if(roles){
            rolesToUse = roles.toList()
        }

        if(permissions) {
            permsToUse = permissions
        }

        SpringUser secUser = SpringUser.of(user, rolesToUse, permsToUse)
        secService.authenticate(secUser)
    }

 }
