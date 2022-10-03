/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler
import org.springframework.security.access.expression.SecurityExpressionOperations
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.FilterInvocation

import yakworks.security.spring.user.SpringUser
import yakworks.security.spring.user.SpringUserInfo
import yakworks.security.user.BasicUserInfo
import yakworks.security.user.CurrentUser
import yakworks.security.user.UserInfo

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class CurrentSpringUser implements CurrentUser {

    @Autowired(required = false)
    AuthenticationTrustResolver authenticationTrustResolver

    @Autowired(required = false)
    AbstractSecurityExpressionHandler securityExpressionHandler

    /**
     * Get the currently logged in UserInfo.
     * @see SpringUser
     * @return the SpringUser UserDetails or null if not logged in.
     */
    @Override
    UserInfo getUser(){
        def user = getAuthentication()?.principal
        if(user && !(user instanceof SpringUserInfo)) {
            if(user instanceof User && ((User)user).username.contains('anonymous')){
                return BasicUserInfo.of("anonymous", ['ANONYMOUS']).id(-1L)
            }
            // else {
            //     //should never get here unless we have something misconfigured
            // }

        } else {
            return user as UserInfo
        }
    }

    @Override
    Serializable getUserId(){
        getUser()?.id ?: 0
    }

    /**
     * Quick check to see if the current user is logged in.
     * calls same method on springSecurityService
     * @return <code>true</code> if the authenticated and not anonymous
     */
    @Override
    boolean isLoggedIn() {
        def authentication = SecurityContextHolder.context.authentication
        return getAuthentication() && !authenticationTrustResolver.isAnonymous(getAuthentication())
    }

    /**
     * Check if current user has any of the specified roles
     */
    @Override
    boolean hasAnyRole(Collection<String> roles){
        getSecurityOperations().hasAnyRole(roles as String[])
    }

    @Override
    boolean hasRole(String role){
        getSecurityOperations().hasRole(role)
    }

    /**
     * Logout current user programmatically
     */
    @Override
    void logout() {
        SecurityContextHolder.context.setAuthentication(null)
        SecurityContextHolder.clearContext()
    }

    /**
     * Get the currently logged in user's <code>Authentication</code>. If not authenticated
     * and the AnonymousAuthenticationFilter is active (true by default) then the anonymous
     * user's auth will be returned (AnonymousAuthenticationToken with username 'anonymousUser' unless overridden).
     * calls same method on springSecurityService
     *
     * @return the authentication
     */
    static Authentication getAuthentication() {
        SecurityContextHolder.context?.authentication
    }

    static Object getPrincipal() {
        getAuthentication()?.getPrincipal()
    }

    SecurityExpressionOperations getSecurityOperations(){
        // def fi = new FilterInvocation('currentUser', 'hasRole')
        // return securityExpressionHandler.createSecurityExpressionRoot(getAuth(), fi)
        //kind of hacky but it works, the strings passed into FilterInvocation ctor are just place holders
        def fi = new FilterInvocation('currentUser', 'hasRole')
        def ctx = securityExpressionHandler.createEvaluationContext(getAuthentication(), fi)
        return (SecurityExpressionOperations)ctx.getRootObject().getValue()
    }

}
