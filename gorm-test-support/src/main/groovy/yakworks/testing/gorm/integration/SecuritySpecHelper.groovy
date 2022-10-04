/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.integration

import groovy.transform.CompileDynamic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import gorm.tools.transaction.WithTrx
import grails.testing.spock.OnceBefore
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
    @OnceBefore
    void setupSecuritySpec() {
        withTrx {
            secService.loginAsSystemUser()
        }
    }

    void authenticate(AppUser user, String... roles) {
        def rolesToUse = user.roles
        if(roles.size()){
            rolesToUse = roles.toList()
        }
        SpringUser secUser = SpringUser.of(user, rolesToUse)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(secUser, user.passwordHash, secUser.authorities)
    }

 }
