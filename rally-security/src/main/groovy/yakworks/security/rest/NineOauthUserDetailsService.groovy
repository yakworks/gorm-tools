/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.rest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.pac4j.core.profile.CommonProfile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

import gorm.tools.security.domain.AppUser
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService

/**
 * Replace the DefaultOauthUserDetailsService
 * Right now we need a user to exists in our DB so this fails the oauth/saml login when the Oauth user doesnt
 * In future we would be smarter about
 */
@Slf4j
@CompileStatic
class NineOauthUserDetailsService implements OauthUserDetailsService {

    @Delegate
    @Autowired
    UserDetailsService userDetailsService

    @Autowired
    UserDetailsChecker preAuthenticationChecks

    @Override
    OauthUser loadUserByUserProfile(CommonProfile userProfile, Collection<GrantedAuthority> defaultRoles) throws UsernameNotFoundException {

        log.debug "Trying to fetch user details for user profile: ${userProfile}"
        OauthUser userDetails = loadUser(userProfile, defaultRoles)

        log.debug "Checking user details with ${preAuthenticationChecks.class.name}"
        preAuthenticationChecks?.check(userDetails)

        return userDetails
    }

    /**
     *  Oauth users may not have password in db,
     *  `org.springframework.security.core.userdetail.User` which is used by security plugins, needs a password or would error from constructor
     *  so here we assign N/A for Oauth user
     *  However the password will never be used for this case, as oauth user does not get authenticated through DaoAuthenticationProvider
     *  See https://github.com/9ci/domain9/issues/962
     */
    @Transactional
    OauthUser loadUser(CommonProfile profile, Collection<GrantedAuthority> defaultRoles) throws UsernameNotFoundException {
        log.debug "loadUserByName(${profile.id}"
        String username = profile.id

        AppUser user = AppUser.getByUsername(username.trim())
        if (!user) {
            throw new UsernameNotFoundException("User not found: $username")
        }
        log.debug "Found user ${user} in the database"

        Collection<GrantedAuthority> authorities = user.roles.collect { new SimpleGrantedAuthority(it.code) }  as Collection<GrantedAuthority>

        Collection<GrantedAuthority> allRoles = (authorities + defaultRoles) as Collection<GrantedAuthority>
        return new OauthUser(user.username, "N/A", allRoles, profile) //no password for oauth users
    }

}
