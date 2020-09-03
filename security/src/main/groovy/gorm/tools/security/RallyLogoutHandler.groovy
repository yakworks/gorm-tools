/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileDynamic

import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler

import grails.plugin.rally.security.UserService
import grails.util.Holders

/**
 * Tracks user logout
 */
@CompileDynamic
class RallyLogoutHandler extends SecurityContextLogoutHandler {

    @Override
    void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        UserService userService = Holders.grailsApplication.mainContext.getBean('userService')
        if (authentication) {
            userService.trackUserLogout()
        }
        super.logout(request, response, authentication)
    }

}
