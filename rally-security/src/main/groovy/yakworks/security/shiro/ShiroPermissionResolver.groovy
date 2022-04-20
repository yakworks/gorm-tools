/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import groovy.transform.CompileStatic

/**
 * Implement this interface and register the class as the 'shiroPermissionResolver' bean.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
interface ShiroPermissionResolver {
    /**
     * Find the permissions granted to the specified user, e.g. using GORM.
     *
     * @param username the username
     * @return zero or more permissions.
     */
    Set<String> resolvePermissions(String username)
}
