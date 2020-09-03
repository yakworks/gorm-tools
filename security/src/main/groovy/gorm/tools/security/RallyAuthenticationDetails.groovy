/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.security.web.authentication.WebAuthenticationDetails

/**
 * Authentication details that stores sources where this authentication comes from.
 *
 * Basically it could be "form" for standard username/password form authentication,
 * and "autologin" for authentication through autologin url.
 *
 * We mainly need this to support autoLogin (/login/autoLogin).
 * In the case of autologin, presented password is hash and should not be encrypted again when verifying passwords.
 * In the case of a regular form login, presented password will be encrypted before verifying password. @See RallyAuthenticationProvider
 *
 * @see RallyAuthenticationDetails#authType
 * @see RallyAuthenticationDetails#isAutoLogin
 */
@Slf4j
@CompileStatic
class RallyAuthenticationDetails extends WebAuthenticationDetails {

    public static final String TYPE_FORM = 'form'
    public static final String TYPE_AUTOLOGIN = 'autologin'

    private String authType

    /**
     * @param request that the authentication request was received from
     */
    RallyAuthenticationDetails(HttpServletRequest request) {
        super(request)
        //set authType from current request
        if (request.requestURI.substring(request.contextPath.length()).startsWith('/login/autoLogin')) {
            //log.info "NineAuthenticationDetails autologin"
            this.authType = TYPE_AUTOLOGIN
        } else {
            this.authType = TYPE_FORM
        }
    }

    boolean isAutoLogin() {
        return authType == TYPE_AUTOLOGIN
    }
}
