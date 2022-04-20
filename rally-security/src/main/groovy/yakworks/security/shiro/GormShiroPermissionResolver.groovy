/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import gorm.tools.security.domain.SecUserPermission
import grails.compiler.GrailsCompileStatic

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@GrailsCompileStatic
class GormShiroPermissionResolver implements ShiroPermissionResolver {

    Set<String> resolvePermissions(String username) {

        SecUserPermission.query {
            eq "user.username", username
        }.projections { property 'permission' }.list() as Set<String>
    }
}
