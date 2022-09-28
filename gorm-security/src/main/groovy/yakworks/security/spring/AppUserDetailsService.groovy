/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import yakworks.security.gorm.AppUserService
import yakworks.security.gorm.model.AppUser

/**
 * Default Gorm-Tools implementation of GrailsUserDetailsService that uses AppUser to load users and roles
 * We dont use the default GormUserDetailsService from Grails Spring Security because we need more flexibility for oauth and ldap.
 * This uses the AppUserService and creates baseline for the various OAuth and Ldap.
 * @see grails.plugin.springsecurity.userdetails.GormUserDetailsService
 */
@Slf4j
@CompileStatic
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

        List<SimpleGrantedAuthority> authorities = null

        if (!loadRoles) {
            authorities = [] //pass empty list
        }
        // password is required so make sure its filled even if its OAuth or ldap
        String passwordHash = user.passwordHash ?: "N/A"

        new SpringUserInfo(user, passwordHash,
            true, !mustChange, true,
            authorities)

    }

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug "loadUserByName(${username})"
        loadUserByUsername username, true
    }

}
