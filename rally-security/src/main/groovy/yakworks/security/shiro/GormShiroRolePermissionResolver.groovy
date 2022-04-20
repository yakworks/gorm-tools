/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import groovy.transform.CompileStatic

import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.RolePermissionResolver
import org.apache.shiro.authz.permission.WildcardPermission

import gorm.tools.security.domain.SecRolePermission

@CompileStatic
class GormShiroRolePermissionResolver implements RolePermissionResolver {

    @Override
    Collection<Permission> resolvePermissionsInRole(String roleString) {
        List<String> stringPermissions = SecRolePermission.executeQuery("""
            Select sr.permission FROM SecRolePermission sr WHERE upper(sr.role.code) = :roleString
        """, [roleString: roleString.toUpperCase()]) as List<String>

        List<Permission> permissions = []

        stringPermissions.each { String perm ->
            permissions << new WildcardPermission(perm)
        }

        return permissions
    }
}
