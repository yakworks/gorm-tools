/*
* Copyright 2013-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.rest

import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer

/**
 * Replaces stock from rest security plugin to add a secure cookie.
 * Generates a JSON response using a {@link AccessTokenJsonRenderer}.
 */
@CompileStatic
class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired AccessTokenJsonRenderer renderer

    @Value('${grails.serverURL:""}')
    String serverURL

    /**
     * Called when a user has been successfully authenticated.
     *
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during the authentication process.
     */
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AccessToken authtoken = authentication as AccessToken
        response.contentType = 'application/json'
        response.characterEncoding = 'UTF-8'
        response.addHeader 'Cache-Control', 'no-store'
        response.addHeader 'Pragma', 'no-cache'
        Cookie cookie = jwtCookie(authtoken.accessToken)
        response.addCookie(cookie)
        response.writer << renderer.generateJson(authentication as AccessToken)
    }

    protected Cookie jwtCookie(String tokenValue) {
        Cookie jwtCookie = new Cookie( 'jwt', tokenValue )
        //FIXME some hard coded values to get it working
        jwtCookie.maxAge = 3600
        jwtCookie.path = '/'
        jwtCookie.setHttpOnly(httpOnly())
        if ( httpOnly() ) {
            jwtCookie.setSecure(true)
        }
        jwtCookie
    }

    protected boolean httpOnly() {
        serverURL?.startsWith('https')
    }
}
