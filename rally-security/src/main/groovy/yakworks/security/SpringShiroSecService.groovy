/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.apache.shiro.SecurityUtils
import org.apache.shiro.web.mgt.WebSecurityManager
import org.grails.web.util.WebUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.WebAttributes

import grails.plugin.springsecurity.SpringSecurityUtils
import yakworks.grails.web.GrailsWebEnvironment
import yakworks.security.shiro.ShiroUtils
import yakworks.security.shiro.SpringSecurityRealm
import yakworks.security.spring.SpringSecService
import yakworks.spring.AppCtx

/**
 * We married shiro and spring security, this does some of the custom wrapping
 * so it it logs in and out of both for example.
 */
@CompileStatic
class SpringShiroSecService<D> extends SpringSecService<D> {

    @Autowired(required = false)
    SpringSecurityRealm springSecurityRealm

    @Autowired(required = false)
    WebSecurityManager shiroSecurityManager

    SpringShiroSecService(Class<D> clazz) {
        super(clazz)
    }

    /**
     * Used in automation to username a bot/system user, also used for tests
     */
    @Override
    void loginAsSystemUser() {
        super.loginAsSystemUser()
        reauthenticateShiro()
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
        SpringSecurityUtils.reauthenticate  username, password
        reauthenticateShiro()
    }

    /**
     * Called after reauthenticate and loginAsSystem user
     */
    void reauthenticateShiro() {
        GrailsWebEnvironment.bindRequestIfNull(AppCtx.ctx)
        def currentRequest = WebUtils.retrieveGrailsWebRequest().currentRequest
        def currentResponse = WebUtils.retrieveGrailsWebRequest().currentResponse
        // request.getSession()
        ShiroUtils.bindSubject SecurityContextHolder.context.authentication,
            springSecurityRealm, shiroSecurityManager, currentRequest, currentResponse
    }

    /**
     * Logout current user programmatically
     */
    void logout() {
        SecurityContextHolder.clearContext()
        SecurityUtils.subject.logout()
    }


    @CompileDynamic
    AuthenticationException getLastAuthenticationException() {
        return WebUtils.retrieveGrailsWebRequest().getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)
    }

}
