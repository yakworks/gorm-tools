/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import groovy.transform.CompileDynamic

import grails.gorm.DetachedCriteria
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

    static List<SecRolePermission> getByRole(long securityRoleId) {
        query([role:[id: securityRoleId]]).list()
    }

    static SecRolePermission get(long securityRoleId, String perm) {
        SecRolePermission.where {
            role == AppUser.load(securityRoleId) && permission == perm
        }.get()
    }

    static SecRolePermission create(SecRole role, String perm, boolean flush = false) {
        def instance = new SecRolePermission(role: role, permission: perm)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(SecRole role, String perm, boolean flush = false) {
        SecRolePermission instance = SecRolePermission.findByRoleAndPermission(role, perm)
        if (!instance) {
            return false
        }

        instance.delete(flush: flush)
        true
    }

    static void removeAll(SecRole role) {
        executeUpdate 'DELETE FROM SecRolePermission WHERE role=:role', [role: role]
    }

    static boolean exists(long securityRoleId) {
        criteriaFor(securityRoleId).count()
    }

    private static DetachedCriteria criteriaFor(long securityRoleId) {
        SecRolePermission.where {
            role == AppUser.load(securityRoleId)
        }
    }

}
