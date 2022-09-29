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
import org.springframework.security.web.FilterInvocation

import yakworks.security.user.CurrentUser

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
     * @see SpringUserInfo
     * @return the SpringUserInfo UserDetails
     */
    @Override
    SpringUserInfo getUserInfo(){
        getAuthentication()?.principal as SpringUserInfo
    }

    @Override
    Serializable getUserId(){
        getUserInfo().id
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
    boolean hasAnyRole(String... roles) {
        getSecurityOperations().hasAnyRole(roles)
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
        getAuthentication().getPrincipal()
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
