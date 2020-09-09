/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler

import gorm.tools.beans.AppCtx
import gorm.tools.security.services.UserService

/**
 * Tracks user logout
 */
@CompileStatic
class SecLogoutHandler extends SecurityContextLogoutHandler {

    @Override
    void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        UserService userService = AppCtx.get('userService', UserService)
        if (authentication) {
            userService.trackUserLogout()
        }
        super.logout(request, response, authentication)
    }

}
