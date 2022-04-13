/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security


import groovy.util.logging.Slf4j

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

import gorm.tools.security.domain.AppUser
import gorm.tools.security.services.AppUserService
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService

@Slf4j
@GrailsCompileStatic
class AppUserDetailsService implements GrailsUserDetailsService {

    @Autowired
    AppUserService userService

    @Transactional
    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        log.debug "loadUserByName(${username}, ${loadRoles})"

        AppUser user = AppUser.getByUsername(username.trim())
        if (!user) {
            throw new UsernameNotFoundException("User not found: $username")
        }
        log.debug "Found user ${user} in the database"

        Boolean mustChange = userService.isPasswordExpired(user)

        /**
         *  Oauth users may not have password in db,
         *  `org.springframework.security.core.userdetail.User` which is used by security plugins, needs a password or would error from constructor
         *  so here we generate a random password on the fly for Oauth user
         *  However the password will never be used for this case, as oauth user does not get authenticated through DaoAuthenticationProvider
         *  See https://github.com/9ci/domain9/issues/962
         */
        String pass = user.passwordHash ?: RandomStringUtils.random(8)

        List<SimpleGrantedAuthority> authorities = []

        if (loadRoles) {
            authorities = user.roles.collect { new SimpleGrantedAuthority(it.springSecRole) }
        }
        new GrailsUser(user.username, pass,
            user.enabled, true, !mustChange, true,
            authorities as Collection<GrantedAuthority>,
            user.id)


    }

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug "loadUserByName(${username})"
        loadUserByUsername username, true
    }

}
