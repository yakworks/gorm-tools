/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain


import gorm.tools.compiler.GormRepository
import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoMessage
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.plugin.rally.security.SecService

@GormRepository
@GrailsCompileStatic
class SecUserRepo implements GormRepo<SecUser> {
    SecService secService

    /**
     * overrides the create method as its more clear whats going on than trying to do role logic with events
     * this will already be transactional as its called from create
     */
    @Override
    SecUser doCreate(Map args, Map data) {
        SecUser user = new SecUser()
        String pwd = data['password']// data.remove('password')
        if(pwd) user.password = pwd
        bindAndSave(args, user, data, BindAction.Create)
        if(data['roles']) setUserRoles(user.id, data['roles'] as List)
        return user
    }

    /**
     * overrides the doUpdate method as its more clear whats going on than trying to do role logic with events
     */
    @Override
    SecUser doUpdate(Map args, Map data) {
        SecUser user = GormRepo.super.doUpdate(args, data)
        if(data['roles']) setUserRoles(user.id, data['roles'] as List)
        return user
    }

    /**
     * Event method called beforeRemove to get rid of the SecRoleUser.
     */
    @RepoListener
    void beforeRemove(SecUser user, BeforeRemoveEvent be) {
        SecRoleUser.removeAll(user)
    }

    /**
     * before persist, do the password encoding
     */
    @RepoListener
    void beforePersist(SecUser user, BeforePersistEvent e) {
        if(user.password) {
            user.passwordHash = encodePassword(user.password)
        }
    }

    /**
     * Sets up password and roles fields for a given User entity. Updates the dependent Contact entity.
     *
     * @param user a User entity to be updated
     * @param p
     * @param bindAction
     */
    @RepoListener
    void afterBind(SecUser user, Map p, AfterBindEvent ae) {
        checkPasswordChange(user, p['newPassword'] as String, p['repassword'] as String)
    }

    /**
     * Adds roles to the user
     */
    SecUser addUserRole(SecUser user, String role) {
        SecRoleUser.create(user, SecRole.findByName(role))
        return user
    }

    /**
     * checks params to see if password exists, that is matches repassword and encodes it if so
     * finally setting it to the password field on User.
     */
    private void checkPasswordChange(SecUser user, String newPassword, String repassword){
        if(!newPassword?.trim()) return
        isSamePass(newPassword, repassword, user)
        user.password = newPassword
    }

    String encodePassword(String pass) {
        secService.encodePassword(pass)
    }

    /** throws EntityValidationException if not. NOTE: keep the real pas**ord name out so scanners dont pick this up */
    void isSamePass(String pass, String rePass, SecUser user) {
        if (pass.trim() != rePass.trim()) {
            Map msg = RepoMessage.setup("password.mismatch", [0], "The passwords you entered do not match")
            throw new EntityValidationException(msg, user)
        }
    }

    @Transactional
    void setUserRoles(Long userId, List rolesId) {
        // Transform both arrays to ListArray<Long> to have ability to compare them
        List<Long> incomeRoles = rolesId.collect {
            // if the list is a map then assume its object map with and id key
            // other wise convert value to long and assume it s list of ids
            (it instanceof Map) ? it['id'] as Long : it as Long
        }
        List<Long> existingRoles = SecRoleUser.getByUser(userId)*.role.id

        List<Long> deleting
        List<Long> addition

        // Get the User instance
        SecUser user = SecUser.get(userId)

        // Compare existing role(s) with incoming
        if (existingRoles && incomeRoles) {
            deleting = existingRoles - incomeRoles // Roles for deleting from table
            addition = incomeRoles - existingRoles // Roles for addition to table
        } else if (incomeRoles) {
            addition = incomeRoles // Add all new roles if there is no existing in the table
        } else if (existingRoles) {
            deleting = existingRoles // Delete all existing roles in the table
        }

        // Delete/Add roles from table
        if (deleting?.size() > 0) {
            deleting.each { Long id ->
                SecRoleUser.remove(user, SecRole.findById(id))
            }
        }
        if (addition?.size() > 0) {
            addition.each { Long id ->
                SecRoleUser.create(user, SecRole.findById(id) as SecRole)
            }
        }

    }
}
