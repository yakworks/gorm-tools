/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileDynamic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

import gorm.tools.transaction.WithTrx
import grails.testing.spock.OnceBefore
import yakworks.security.SecService
import yakworks.security.gorm.model.AppUser
import yakworks.security.spring.SpringSecUser

@CompileDynamic
trait SecuritySpecHelper implements WithTrx{

    @Autowired @Qualifier("secService")
    SecService secService

    //need to name it like this, otherwise subclasses cant use setupSpec method
    @OnceBefore
    void setupSecuritySpec() {
        withTrx {
            secService.loginAsSystemUser()
        }
    }

    void authenticate(AppUser user, String... roles) {
        roles = roles.collect { it}
        List authorities = AuthorityUtils.createAuthorityList(roles)

        SpringSecUser secUser = new SpringSecUser(user.username, user.passwordHash, user.enabled, true, !user.passwordExpired, true, authorities, user.id)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(secUser, user.passwordHash, authorities)
    }

 }
