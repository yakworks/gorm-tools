/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.services

import javax.servlet.http.HttpSession

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

import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUser

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class SpringSecService<D> implements SecService<D>{

    @Autowired(required = false) //required = false so this bean works in case security. active is false
    SpringSecurityService springSecurityService

    @Autowired(required = false)
    AuthenticationTrustResolver authenticationTrustResolver

    SpringSecService(Class<D> clazz) {
        this.entityClass = clazz
    }
    /**
     * Get the currently logged in user's principal. If not authenticated and the
     * AnonymousAuthenticationFilter is active (true by default) then the anonymous
     * user's name will be returned ('anonymousUser' unless overridden).
     * calls same method on springSecurityService
     *
     * @return the principal
     */
    def getPrincipal() {
        springSecurityService.getPrincipal()
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
        springSecurityService.getAuthentication()
    }

    /**
     * Gets the currently logged in user id from principal
     */
    @Override
    Long getUserId() {
        def curPrincipal = getPrincipal()
        if (curPrincipal instanceof GrailsUser) {
            return (curPrincipal as GrailsUser).id as Long
        } else if (curPrincipal instanceof User) {
            //has to be User, might be Oauth. So lookup by Username
            getUserIdByUsername((curPrincipal as User).username) as Long
        }
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
     * Quick check to see if the current user is logged in.
     * calls same method on springSecurityService
     * @return <code>true</code> if the authenticated and not anonymous
     */
    @Override
    boolean isLoggedIn() {
        return springSecurityService.isLoggedIn()
    }

    /**
     * Used in automation to username a bot/system user, also used for tests
     */
    @Override
    void loginAsSystemUser() {
        AppUser user = AppUser.get(1)
        assert user
        List<GrantedAuthority> authorities = parseAuthoritiesString([SecRole.ADMIN] as String[])
        GrailsUser grailsUser = new GrailsUser(user.username, user.passwordHash, user.enabled, true, !user.passwordExpired, true, authorities, user.id)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, user.passwordHash, authorities)
    }

    /**
     * Verify that user has logged in fully (ie has presented username/password) and is not logged in using rememberMe cookie
     */
    boolean isAuthenticatedFully() {
        return (isLoggedIn() && !isRememberMe())
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

    /**
     * Check if current user has any of the specified roles
     */
    @Override
    boolean ifAnyGranted(String... roles) {
        return SpringSecurityUtils.ifAnyGranted(parseAuthoritiesString(roles))
    }

    /**
     * Check if current user has all of the specified roles
     */
    @Override
    boolean ifAllGranted(String... roles) {
        return SpringSecurityUtils.ifAllGranted(parseAuthoritiesString(roles))
    }

    /**
     * Check if current user has none of the specified roles
     */
    boolean ifNotGranted(String... roles) {
        return SpringSecurityUtils.ifNotGranted(parseAuthoritiesString(roles))
    }

    /**
     * Get the current user's roles.
     * @return a list of roles (empty if not authenticated).
     */
    List<String> getPrincipalRoles() {
        if (!isLoggedIn()) return []
        def roles = user['roles'] as Set<SecRole>
        return roles*.name
    }

    /**
     * Logout current user programmatically
     */
    void logout() {
        HttpSession session = WebUtils.retrieveGrailsWebRequest().currentRequest.getSession(false)
        if (session) {
            session.invalidate()
        }
        SecurityContextHolder.context.setAuthentication(null)
        SecurityContextHolder.clearContext()
    }


    @CompileDynamic
    AuthenticationException getLastAuthenticationException() {
        return WebUtils.retrieveGrailsWebRequest().getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)
    }

}
