/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.Realm
import org.apache.shiro.util.ThreadContext
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Fires on every request and configures Shiro auth based on Spring Security auth.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
class ShiroSubjectBindingFilter extends OncePerRequestFilter {

    /** Dependency injection for the AuthenticationTrustResolver. */
    AuthenticationTrustResolver authenticationTrustResolver

    /** Dependency injection for the realm. */
    Realm realm

    /** Dependency injection for the security manager. */
    SecurityManager securityManager

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            bind request, response
            chain.doFilter request, response
        }
        finally {
            unbind()
        }
    }

    protected void bind(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.context.authentication
        if (!authentication || authenticationTrustResolver.isAnonymous(authentication)) {
            ThreadContext.bind securityManager
        }
        else {
            ShiroUtils.bindSubject authentication, realm, securityManager, request, response
        }
    }

    protected void unbind() {
        ThreadContext.unbindSubject()
        ThreadContext.unbindSecurityManager()
    }
}
