/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.testing

import groovy.transform.CompileDynamic

import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

import gorm.tools.security.domain.SecUser
import gorm.tools.security.services.SpringSecService
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.userdetails.GrailsUser

@CompileDynamic
trait SecuritySpecUnitTestHelper {

    Closure doWithSpringFirst() {
        return {
            passwordEncoder(PlaintextPasswordEncoder)
            springSecurityService(SpringSecurityService) { bean ->
                bean.autowire = "byName"
            }
            authenticationTrustResolver(AuthenticationTrustResolverImpl)
            secService(SpringSecService, SecUser)
        }
    }

    void authenticate(SecUser user, String... roles) {
        roles = roles.collect { "ROLE_" + it}
        List authorities = AuthorityUtils.createAuthorityList(roles)

        GrailsUser grailsUser = new GrailsUser(user.username, user.passwordHash, user.enabled, true, !user.mustChangePassword, true, authorities, user.id)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, user.passwordHash, authorities)
    }

}
