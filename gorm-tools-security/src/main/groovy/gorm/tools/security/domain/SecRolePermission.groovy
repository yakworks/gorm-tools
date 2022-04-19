package gorm.tools.security.domain


import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class SecRolePermission implements RepoEntity<SecRolePermission>, Serializable  {
    SecRole role
    String permission

    SecRolePermission(){}

    SecRolePermission(SecRole role, String permission) {
        this.role = role
        this.permission = permission
    }

    static constraints = {
        permission unique: 'role'
    }

    static mapping = {
        id composite: ['role', 'permission']
        version false
        role column:'roleId'
    }

    @Override
    boolean equals(Object other) {
        if (other instanceof SecRolePermission) {
            other.permission == permission && other.roleId == role?.id
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (permission) {
            hashCode = HashCodeHelper.updateHash(hashCode, permission)
        }
        if (role) {
            hashCode = HashCodeHelper.updateHash(hashCode, role.id)
        }
        hashCode
    }
}
