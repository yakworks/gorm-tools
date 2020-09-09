/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import groovy.transform.CompileStatic

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent

import gorm.tools.security.domain.SecUser
import gorm.tools.security.services.UserService
import grails.plugin.springsecurity.userdetails.GrailsUser

/**
 * Springsecurity login handler
 * tracks user logins and sets flag if password expiry warning should be displayed.
 */
@CompileStatic
class SecLoginHandler implements ApplicationListener<AbstractAuthenticationEvent> {
    @Autowired
    UserService userService

    @Value('${gorm.tools.security.password.expireEnabled:false}')
    boolean passwordExpiryEnabled

    @Value('${gorm.tools.security.password.warnDays:30}')
    int passwordWarnDays

    void onApplicationEvent(AbstractAuthenticationEvent event) {
        if (event instanceof AuthenticationSuccessEvent) {
            if (shouldWarnAboutPasswordExpiry((GrailsUser) event.authentication.principal)) {
                GrailsWebRequest webRequest = WebUtils.retrieveGrailsWebRequest()
                if (webRequest) {
                    webRequest.currentRequest.session['warnAboutPasswordExpiry'] = true
                }
            }
        }
        if (event instanceof InteractiveAuthenticationSuccessEvent) {
            trackLogin((InteractiveAuthenticationSuccessEvent) event)
        }
    }

    void trackLogin(InteractiveAuthenticationSuccessEvent event) {
        userService.trackUserLogin()
    }

    boolean shouldWarnAboutPasswordExpiry(GrailsUser principal) {
        boolean result = false
        SecUser.withTransaction {
            if (passwordExpiryEnabled) {
                int warnBeforeDays = passwordWarnDays
                int remainingDays = userService.remainingDaysForPasswordExpiry(SecUser.get((Long) principal.id))
                if (warnBeforeDays >= remainingDays) result = true
            }
        }

        return result
    }

}
