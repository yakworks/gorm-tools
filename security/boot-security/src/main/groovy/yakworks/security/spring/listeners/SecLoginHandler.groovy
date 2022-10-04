/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.listeners

import groovy.transform.CompileStatic

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent

import yakworks.security.gorm.AppUserService
import yakworks.security.gorm.model.AppUser
import yakworks.security.spring.user.SpringUserInfo

/**
 * Springsecurity username handler
 * tracks user logins and sets flag if password expiry warning should be displayed.
 */
@CompileStatic
class SecLoginHandler implements ApplicationListener<AbstractAuthenticationEvent> {

    @Autowired AppUserService userService

    @Value('${yakworks.security.password.expireEnabled:false}')
    boolean passwordExpiryEnabled

    @Value('${yakworks.security.password.warnDays:30}')
    int passwordWarnDays

    void onApplicationEvent(AbstractAuthenticationEvent event) {
        //do password check only if local user, not if a federated user, eg from Okta, in which case it would be OauthUser.
        def principal = event.authentication.principal
        if (event instanceof AuthenticationSuccessEvent && principal instanceof SpringUserInfo) {
            Serializable uid = ((SpringUserInfo)principal).id
            if (shouldWarnAboutPasswordExpiry(uid)) {
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

    boolean shouldWarnAboutPasswordExpiry(Serializable userId) {
        if (passwordExpiryEnabled) {
            int remainingDays = userService.remainingDaysForPasswordExpiry(AppUser.get(userId))
            if (passwordWarnDays >= remainingDays) return true
        }
        return false
    }

}
