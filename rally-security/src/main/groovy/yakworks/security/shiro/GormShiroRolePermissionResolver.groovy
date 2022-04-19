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
        if (roleString.startsWith('ROLE_')) roleString = roleString.substring('ROLE_'.length())
        List<String> stringPermissions = SecRolePermission.executeQuery("""
            Select sr.permission FROM SecRolePermission sr WHERE lower(sr.role.name) = :roleString
        """, [roleString: roleString.toLowerCase()]) as List<String>

        List<Permission> permissions = []

        stringPermissions.each { String perm ->
            permissions << new WildcardPermission(perm)
        }

        return permissions
    }
}
