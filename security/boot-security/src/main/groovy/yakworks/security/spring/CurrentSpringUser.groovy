/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileStatic

import org.apache.shiro.authz.permission.WildcardPermission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.expression.SecurityExpressionHandler
import org.springframework.security.access.expression.SecurityExpressionOperations
import org.springframework.security.authentication.AnonymousAuthenticationToken
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

    // @Autowired(required = false)
    // AbstractSecurityExpressionHandler securityExpressionHandler
    @Autowired SecurityExpressionHandler<FilterInvocation> securityExpressionHandler

    /**
     * Get the currently logged in UserInfo.
     * @see SpringUser
     * @return the SpringUser thats stored in Authentication.details from AuthSuccessUserInfoListener
     */
    @Override
    UserInfo getUser(){
        def principal = getAuthentication()?.principal
        def user = getAuthentication()?.details
        if(principal instanceof String && principal == "anonymousUser") {
            return BasicUserInfo.of("anonymous", ['ANONYMOUS']).id(-1L)
        } else {
            return user as UserInfo
        }
    }
    // @Override
    // UserInfo getUser(){
    //     def user = getAuthentication()?.principal
    //     if(user && !(user instanceof SpringUserInfo)) {
    //         if(user instanceof User && ((User)user).username.contains('anonymous')){
    //             return BasicUserInfo.of("anonymous", ['ANONYMOUS']).id(-1L)
    //         }
    //     } else {
    //         return user as UserInfo
    //     }
    // }


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
        def authentication = getAuthentication()
        return (authentication != null && !(authentication instanceof AnonymousAuthenticationToken))
    }

    //FIXME using springs does not work as in many cases the Authentication doesnt have the roles.
    // this will be a problem if we move to using the annotations so this is a temp hack.
    // can see problem by setting breakpoint in AuthSuccessUserInfoListener and inspecting the Authentication objects authorities.

    // @Override
    // boolean hasAnyRole(Collection<String> roles){
    //     getSecurityOperations().hasAnyRole(roles as String[])
    // }
    // @Override
    // boolean hasRole(String role){
    //     getSecurityOperations().hasRole(role)
    // }

    /**
     * Logout current user programmatically
     */
    @Override
    void logout() {
        SecurityContextHolder.context.setAuthentication(null)
        SecurityContextHolder.clearContext()
    }

    @Override
    boolean hasPermission(String requiredPermission) {
        return getUser().permissions.any { def userPerm ->
            return toWildcardPermission((String)userPerm).implies(toWildcardPermission(requiredPermission))
        }
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

    protected WildcardPermission toWildcardPermission(String perm) {
        return new WildcardPermission(perm)
    }

}
