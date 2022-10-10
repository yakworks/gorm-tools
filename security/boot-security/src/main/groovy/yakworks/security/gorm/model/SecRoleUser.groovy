/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import groovy.transform.CompileDynamic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.gorm.DetachedCriteria
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class SecRoleUser implements RepoEntity<SecRoleUser>, Serializable {

    AppUser user
    SecRole role

    static transients = ['roleName', 'userName']

    static mapping = {
        cache "nonstrict-read-write"
        id composite: ['user', 'role']
        version false
        user column:'userId'
        role column:'secRoleId'
    }

    static constraintsMap= [
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

    static SecRoleUser get(long userId, long roleId) {
        criteriaFor(userId, roleId).get()
    }

    static SecRoleUser get(long userId, String roleCode) {
        Long roleId = SecRole.getByCode(roleCode).id
        criteriaFor(userId, roleId).get()
    }

    static SecRoleUser create(AppUser user, SecRole role, boolean flush = false) {
        def instance = new SecRoleUser(user: user, role: role)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(AppUser user, SecRole role, boolean flush = false) {
        SecRoleUser instance = SecRoleUser.findByUserAndRole(user, role)
        if (!instance) {
            return false
        }

        instance.delete(flush: flush)
        true
    }

    static void removeAll(AppUser user) {
        executeUpdate 'DELETE FROM SecRoleUser WHERE user=:user', [user: user]
    }

    static void removeAll(SecRole role) {
        executeUpdate 'DELETE FROM SecRoleUser WHERE role=:role', [role: role]
    }

    static boolean exists(long userId, long securityRoleId) {
        criteriaFor(userId, securityRoleId).count()
    }

    private static DetachedCriteria criteriaFor(long userId, long securityRoleId) {
        SecRoleUser.where {
            user == AppUser.load(userId) &&
                role == SecRole.load(securityRoleId)
        }
    }

    @CompileDynamic
    static Map<SecRole, Boolean> getRoleMap(AppUser userInstance) {
        List roles = SecRole.list()
        Set userRoleNames = []
        if (userInstance.getId()) {
            for (r in userInstance.roles) {
                userRoleNames << r
            }
        }
        Map<SecRole, Boolean> roleMap = [:]
        for (r in roles) {
            roleMap[(r)] = userRoleNames.contains(r.code)
        }

        return roleMap
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
