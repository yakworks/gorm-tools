/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.testing

import groovy.transform.CompileDynamic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

import gorm.tools.security.domain.SecUser
import grails.plugin.rally.security.SecService
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.testing.spock.OnceBefore

@CompileDynamic
trait SecuritySpecHelper {
    @Autowired
    @Qualifier("secService")
    SecService secService

    //need to name it like this, otherwise subclasses cant use setupSpec method
    @OnceBefore
    void setupSecuritySpec() {
        secService.loginAsSystemUser()
    }

    void authenticate(SecUser user, String... roles) {
        roles = roles.collect { "ROLE_" + it}
        List authorities = AuthorityUtils.createAuthorityList(roles)

        GrailsUser grailsUser = new GrailsUser(user.login, user.passwd, user.enabled, true, !user.mustChangePassword, true, authorities, user.id)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, user.passwd, authorities)
    }

 }
