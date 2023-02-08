/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import groovy.transform.CompileDynamic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@Entity
@GrailsCompileStatic
class SecRoleUser implements GormRepoEntity<SecRoleUser, SecRoleUserRepo>, Serializable {

    AppUser user
    SecRole role

    static transients = ['roleName', 'userName']

    // static mappingOld = {
    //     cache "nonstrict-read-write"
    //     id composite: ['user', 'role']
    //     version false
    //     user column:'userId'
    //     role column:'secRoleId'
    // }

    static mapping = orm {
        cache "nonstrict-read-write"
        id composite('user', 'role')
        version false
        columns(
            user: property(column:'userId'),
            role: property(column:'secRoleId')
        )
    }

    static constraintsMap = [
        user: [d: 'The user for the role'],
        role: [d: 'The role for the user'],
        userId: [d: 'The user id to assign the role'],
        roleId: [d: 'The role id']
    ]


    String getRoleName() {
        this.role.name
    }

    String getUserName() {
        this.user.name
    }

    static List<SecRoleUser> getByUser(long userId) {
        query([user:[id: userId]]).list()
    }

    static SecRoleUser get(long roleId, long userId) {
        getRepo().get(SecRole.load(roleId), AppUser.load(userId))
    }

    static SecRoleUser get(String roleCode, long userId) {
        getRepo().get(SecRole.getByCode(roleCode), AppUser.load(userId))
    }

    static SecRoleUser create(SecRole role, AppUser user, Map args = [:]) {
        getRepo().create(role, user, args)
    }

    //legacy flipped
    static SecRoleUser create(AppUser user, SecRole role, boolean flush = false) {
        create(role, user, [flush: flush])
    }

    static void remove(SecRole role, AppUser user) {
        getRepo().remove(role, user)
    }

    static void removeAll(AppUser user) {
        executeUpdate 'DELETE FROM SecRoleUser WHERE user=:user', [user: user]
    }

    static void removeAll(SecRole role) {
        executeUpdate 'DELETE FROM SecRoleUser WHERE role=:role', [role: role]
    }

    static boolean exists(long securityRoleId, long userId) {
        getRepo().exists(SecRole.load(securityRoleId), AppUser.load(userId))
    }

    @Override
    boolean equals(Object other) {
        if (other instanceof SecRoleUser) {
            other.userId == user?.getId() && other.roleId == role?.getId()
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (user) {
            hashCode = HashCodeHelper.updateHash(hashCode, user.getId())
        }
        if (role) {
            hashCode = HashCodeHelper.updateHash(hashCode, role.getId())
        }
        hashCode
    }


}
