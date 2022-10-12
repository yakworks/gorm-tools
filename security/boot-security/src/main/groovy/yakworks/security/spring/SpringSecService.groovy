/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserCache
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

import jakarta.annotation.Nullable
import yakworks.security.Roles
import yakworks.security.SecService
import yakworks.security.spring.user.GetUserById
import yakworks.security.spring.user.SpringUserInfo
import yakworks.security.spring.user.SpringUserUtils
import yakworks.security.user.UserInfo

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class SpringSecService implements SecService {

    @Autowired(required=false)
    AuthenticationTrustResolver authenticationTrustResolver

    @Autowired(required=false)
    UserDetailsService userDetailsService

    @Autowired(required=false)
    UserCache userCache

    /**
     * Get the currently logged in user's <code>Authentication</code>. If not authenticated
     * and the AnonymousAuthenticationFilter is active (true by default) then the anonymous
     * user's auth will be returned (AnonymousAuthenticationToken with username 'anonymousUser'\
     * unless overridden).
     * calls same method on springSecurityService
     *
     * @return the authentication
     */
    Authentication getAuthentication() {
        CONTEXT.authentication
    }

    static SecurityContext getCONTEXT() {
        return SecurityContextHolder.context
    }

    /**
     * Used in automation to username a bot/system user, also used for tests
     */
    @Override
    UserInfo loginAsSystemUser() {
        SpringUserInfo secUser = SpringUserUtils.systemUser() //((AppUserDetailsService)userDetailsService).loadUserByUserId(1)
        assert secUser.roles.contains(Roles.ADMIN)
        authenticate(secUser)
        return secUser
    }

    /**
     * Pass any object that is a UserInfo. BasicUserInfo.of("dagny") for example.
     * Must also implement UserDetails from spring to get the authorities
     * Removes the user from the user cache if it exists to force a refresh at next username.
     *
     * @param username the user's username name
     * @param password optional
     */
    @Override
    UserInfo login(String username, @Nullable String password) {
        SpringUserInfo secUser = (SpringUserInfo)userDetailsService.loadUserByUsername(username)
        authenticate(secUser)
        return secUser
    }

    /**
     * Pass any object that is a UserInfo. BasicUserInfo.of("dagny") for example.
     * Must also implement UserDetails from spring to get the authorities
     * Removes the user from the user cache if it exists to force a refresh at next username.
     *
     * @param userInfo the userInfo that is also a UserDetails
     */
    @Override
    UserInfo authenticate(UserInfo userInfo) {
        Collection<? extends GrantedAuthority> grantedAuthories
        if(userInfo instanceof UserDetails) grantedAuthories = ((UserDetails)userInfo).authorities
        def upauth = new UsernamePasswordAuthenticationToken(userInfo, userInfo.passwordHash, grantedAuthories)
        upauth.details = userInfo
        CONTEXT.authentication = upauth
        //before or after?
        userCache?.removeUserFromCache userInfo.username
        return userInfo
    }

    /**
     * is a user logged in
     */
    boolean isLoggedIn(){
        currentUser.isLoggedIn()
    }

    /**
     * Check if user is logged in using rememberMe cookie.
     */
    boolean isRememberMe() {
        return authenticationTrustResolver.isRememberMe(SecurityContextHolder.context?.authentication)
    }

    @Override
    UserInfo getUser(Serializable uid) {
        ((GetUserById)userDetailsService).getById(uid)
    }
}
