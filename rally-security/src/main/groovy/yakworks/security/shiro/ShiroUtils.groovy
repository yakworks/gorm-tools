/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.Realm
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.support.SubjectThreadState
import org.apache.shiro.web.session.HttpServletSession
import org.apache.shiro.web.subject.support.WebDelegatingSubject
import org.springframework.security.core.Authentication

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@Slf4j
@CompileStatic
class ShiroUtils {

    protected ShiroUtils() {
        // static only
    }

    static void bindSubject(Authentication authentication, Realm realm, SecurityManager securityManager,
            HttpServletRequest request, HttpServletResponse response) {

        String host = request.remoteHost
        String username = authentication.principal['username']

        WebDelegatingSubject subject = new WebDelegatingSubject(
                new SimplePrincipalCollection(username, realm.name), true, host,
                new HttpServletSession(request.getSession(), host),
                true, request, response, securityManager)

        log.debug 'Binding subject for principal {} from host {}', username, host

        new SubjectThreadState(subject).bind()
    }
}
