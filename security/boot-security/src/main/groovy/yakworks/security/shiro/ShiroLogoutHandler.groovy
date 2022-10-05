/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.apache.shiro.SecurityUtils
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler

/**
 * Spring Sec LogoutHandler that will call Shiro logout too.
 */
@CompileStatic
class ShiroLogoutHandler implements LogoutHandler {
    void logout(HttpServletRequest req, HttpServletResponse res, Authentication a) {
        SecurityUtils.subject.logout()
    }
}
