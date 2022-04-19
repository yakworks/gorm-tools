package gorm.tools.security.domain


import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class SecUserPermission implements RepoEntity<SecUserPermission>, Serializable  {
    AppUser user
    String permission

    SecUserPermission() {}

    SecUserPermission(AppUser user, String permission) {
        this.user = user
        this.permission = permission
    }

    static constraints = {
        permission nullable: false
    }

    static mapping = {
        id composite: ['user', 'permission']
        version false
        user column:'userId'
    }

    @Override
    boolean equals(Object other) {
        if (other instanceof SecUserPermission) {
            other.userId == user?.id && other.permission == permission
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (user) {
            hashCode = HashCodeHelper.updateHash(hashCode, user.id)
        }
        if (permission) {
            hashCode = HashCodeHelper.updateHash(hashCode, permission)
        }
        hashCode
    }
}
