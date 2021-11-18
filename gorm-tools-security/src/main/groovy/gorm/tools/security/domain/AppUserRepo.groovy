/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import javax.annotation.Nullable
import javax.inject.Inject

import org.springframework.security.crypto.password.PasswordEncoder

import gorm.tools.api.EntityValidationProblem
import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import yakworks.i18n.MsgKey

@GormRepository
@GrailsCompileStatic
class AppUserRepo implements GormRepo<AppUser> {
    /** dependency injection for the password encoder */
    @Inject @Nullable
    PasswordEncoder passwordEncoder
    // SecService secService

    /**
     * overrides the bindAndCreate method vs events
     * as its more clear whats going on than trying to do role logic with events
     * this will already be transactional as its called from create
     */
    @Override
    void bindAndCreate(AppUser user, Map data, Map args) {
        String pwd = data['password']// data.remove('password')
        if(pwd) user.password = pwd
        bindAndSave(user, data, BindAction.Create, args)
        if(data['roles']) setUserRoles(user.id, data['roles'] as List)
    }

    /**
     * overrides the doUpdate method as its more clear whats going on than trying to do role logic with events
     */
    @Override
    AppUser doUpdate(Map data, Map args) {
        AppUser user = GormRepo.super.doUpdate(data, args)
        if(data['roles']) setUserRoles(user.id, data['roles'] as List)
        return user
    }

    /**
     * Event method called beforeRemove to get rid of the SecRoleUser.
     */
    @RepoListener
    void beforeRemove(AppUser user, BeforeRemoveEvent be) {
        SecRoleUser.removeAll(user)
    }

    /**
     * before persist, do the password encoding
     */
    @RepoListener
    void beforePersist(AppUser user, BeforePersistEvent e) {
        if(user.password) {
            user.passwordHash = encodePassword(user.password)
        }
        if(!user.name) user.name = user.username
    }

    /**
     * Sets up password and roles fields for a given User entity. Updates the dependent Contact entity.
     *
     * @param user a User entity to be updated
     * @param p the params with newPassword and repassword
     */
    @RepoListener
    void afterBind(AppUser user, Map p, AfterBindEvent ae) {
        checkPasswordChange(user, p['newPassword'] as String, p['repassword'] as String)
    }

    /**
     * Adds roles to the user
     */
    AppUser addUserRole(AppUser user, String role) {
        SecRoleUser.create(user, SecRole.findByName(role))
        return user
    }

    /**
     * checks params to see if password exists, that is matches repassword and encodes it if so
     * finally setting it to the password field on User.
     */
    private void checkPasswordChange(AppUser user, String newPassword, String repassword){
        if(!newPassword?.trim()) return
        isSamePass(newPassword, repassword, user)
        user.password = newPassword
    }

    String encodePassword(String pass) {
        passwordEncoder.encode pass
    }

    /** throws EntityValidationException if not. NOTE: keep the real pas**ord name out so scanners dont pick this up */
    void isSamePass(String pass, String rePass, AppUser user) {
        if (pass.trim() != rePass.trim()) {
            def msgKey = MsgKey.of('password.mismatch').fallbackMessage("The passwords you entered do not match")
            throw EntityValidationProblem.of(msgKey).entity(user)
        }
    }

    @Transactional
    void setUserRoles(Long userId, List rolesId) {
        // Transform both arrays to ListArray<Long> to have ability to compare them
        List<Long> incomeRoles = rolesId.collect {
            // if the list is a map then assume its object map with and id key
            // otherwise convert value to long and assume it s list of ids
            (it instanceof Map) ? it['id'] as Long : it as Long
        }
        List<Long> existingRoles = SecRoleUser.getByUser(userId)*.role.id

        List<Long> deleting
        List<Long> addition

        // Get the User instance
        AppUser user = AppUser.get(userId)

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
