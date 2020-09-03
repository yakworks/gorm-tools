/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.springframework.security.authentication.AuthenticationDetailsSource

//Custom authentication source to create RallyAuthenticationDetails - we need this to support autologin (/login/autoLogin)
@CompileStatic
class RallyAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, RallyAuthenticationDetails> {

    @Override
    RallyAuthenticationDetails buildDetails(HttpServletRequest request) {
        return new RallyAuthenticationDetails(request)
    }
}
