/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import groovy.transform.CompileStatic

import org.apache.shiro.SecurityUtils
import org.springframework.security.core.context.SecurityContextHolder

import yakworks.security.spring.CurrentSpringUser

/**
 * Spring implementation of the generic base SecService
 */
@CompileStatic
class CurrentSpringShiroUser extends CurrentSpringUser {

    /**
     * Logout current user programmatically
     */
    @Override
    void logout() {
        SecurityContextHolder.context.setAuthentication(null)
        SecurityContextHolder.clearContext()
        SecurityUtils.subject.logout()
    }

}
