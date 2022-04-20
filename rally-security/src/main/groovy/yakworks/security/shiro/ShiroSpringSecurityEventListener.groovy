/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.Realm
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent

import grails.plugin.springsecurity.web.SecurityRequestHolder

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class ShiroSpringSecurityEventListener implements ApplicationListener<AbstractAuthenticationEvent> {

    /** Dependency injection for the realm. */
    Realm realm

    /** Dependency injection for the security manager. */
    SecurityManager securityManager

    @SuppressWarnings(['EmptyIfStatement'])
    void onApplicationEvent(AbstractAuthenticationEvent event) {

        log event

        if (event instanceof AuthenticationSuccessEvent || event instanceof InteractiveAuthenticationSuccessEvent) {
            ShiroUtils.bindSubject event.authentication, realm, securityManager,
                    SecurityRequestHolder.request, SecurityRequestHolder.response
        }
        else if (event instanceof AuthenticationSwitchUserEvent) {
            // TODO
        }
    }

    protected void log(AbstractAuthenticationEvent event) {
        log.debug 'on{} for Authentication {}', event.authentication, event.class.simpleName
    }
}
