/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.log.LogMessage
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AbstractOAuth2Token
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler

import yakworks.security.spring.token.generator.TokenGenerator

/**
 * Success handler that will add cookie for token first before doing redirects.
 * Also add token to end of url when it ends with an =, legacy to get rcm-ui working
 * see app.security.frontendCallbackUrl
 */
@CompileStatic
class CookieUrlTokenSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler{

    @Autowired TokenGenerator tokenGenerator

    @Override
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        AbstractOAuth2Token token = tokenGenerator.generate()
        Cookie cookie = TokenUtils.tokenCookie(request, token)
        response.addCookie(cookie)
        // if(defaultTargetUrl.endsWith("=")){
        //
        // }
        String targetUrl = defaultTargetUrl
        if(targetUrl.endsWith("=")){
            targetUrl = "${targetUrl}${token.tokenValue}"
        }
        this.redirectStrategy.sendRedirect(request, response, targetUrl)

        //super.onAuthenticationSuccess(request, response, authentication);
    }

}
