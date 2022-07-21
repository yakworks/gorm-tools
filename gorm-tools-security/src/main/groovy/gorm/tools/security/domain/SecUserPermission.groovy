/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
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
        cache "nonstrict-read-write"
        id composite: ['user', 'permission']
        version false
        user column:'userId'
    }

    @Override
    boolean equals(Object other) {
        if (other instanceof SecUserPermission) {
            other.userId == user?.getId() && other.permission == permission
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (user) {
            hashCode = HashCodeHelper.updateHash(hashCode, user.getId())
        }
        if (permission) {
            hashCode = HashCodeHelper.updateHash(hashCode, permission)
        }
        hashCode
    }
}
