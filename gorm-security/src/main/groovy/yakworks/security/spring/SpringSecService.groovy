/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.web.util.WebUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.WebAttributes

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import yakworks.security.SecService
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.user.BasicUserInfo
import yakworks.security.user.UserInfo

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class SpringSecService implements SecService<AppUser> {

    @Autowired(required = false) //required = false so this bean works in case security. active is false
    SpringSecurityService springSecurityService

    @Autowired(required = false)
    AuthenticationTrustResolver authenticationTrustResolver

    SpringSecService() {
        this.entityClass = AppUser
    }

    /**
     * Get the currently logged in user's principal. If not authenticated and the
     * AnonymousAuthenticationFilter is active (true by default) then the anonymous
     * user's name will be returned ('anonymousUser' unless overridden).
     * calls same method on springSecurityService
     * @see SpringUserInfo
     * @return the principal (which as we have setup is the SpringUserInfo
     */
    def getPrincipal() {
        getAuthentication()?.principal
    }

    UserInfo getUserInfo(){
        getPrincipal() as UserInfo
    }

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
        SecurityContextHolder.context?.authentication
    }

    /**
     * Encode the password using the configured PasswordEncoder.
     * calls same method on springSecurityService
     */
    @Override
    String encodePassword(String password) {
        springSecurityService.encodePassword(password)
    }

    /**
     * Used in automation to username a bot/system user, also used for tests
     */
    @Override
    void loginAsSystemUser() {
        AppUser user = AppUser.get(1)
        assert user
        List<GrantedAuthority> authorities = parseAuthoritiesString([SecRole.ADMIN] as String[])
        SpringUserInfo secUser = SpringUserInfo.of(user)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(secUser, user.passwordHash, secUser.authorities)
    }

    /**
     * Rebuild an Authentication for the given username and register it in the security context.
     * Typically used after updating a user's authorities or other auth-cached info.
     * <p/>
     * Also removes the user from the user cache to force a refresh at next username.
     * calls same on springSecurityService
     *
     * @param username the user's username name
     * @param password optional
     */
    @Override
    void reauthenticate(String username, String password = null) {
        springSecurityService.reauthenticate username, password
    }

    /**
     * Check if user is logged in using rememberMe cookie.
     */
    boolean isRememberMe() {
        return authenticationTrustResolver.isRememberMe(SecurityContextHolder.context?.authentication)
    }

    private static List<GrantedAuthority> parseAuthoritiesString(String[] roleNames) {
        List<GrantedAuthority> requiredAuthorities = []
        for (String auth : roleNames) {
            auth = auth.trim()
            if (auth.length() > 0) {
                requiredAuthorities.add(new SimpleGrantedAuthority(auth.toUpperCase()))
            }
        }

        return requiredAuthorities
    }

    @CompileDynamic
    AuthenticationException getLastAuthenticationException() {
        return WebUtils.retrieveGrailsWebRequest().getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)
    }

    @CompileDynamic
    static SpringUserInfo mockUser(String username, String pwd, List roles, Long id, Long orgId){
        def u = new BasicUserInfo(username: username, passwordHash: pwd, roles: roles, id: id, orgId: orgId)
        SpringUserInfo.of(u)
    }

}
