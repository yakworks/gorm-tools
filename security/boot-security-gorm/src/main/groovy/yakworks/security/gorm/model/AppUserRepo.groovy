/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.Errors

import gorm.tools.databinding.BindAction
import gorm.tools.mango.jpql.KeyExistsQuery
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.commons.lang.Validate
import yakworks.security.PasswordConfig
import yakworks.security.services.PasswordValidator

@GormRepository
@GrailsCompileStatic
class AppUserRepo extends LongIdGormRepo<AppUser> {
    @Autowired PasswordEncoder passwordEncoder
    @Autowired PasswordConfig passwordConfig
    @Autowired PasswordValidator passwordValidator


    //cached instance of the query for id to keep it fast
    KeyExistsQuery usernameExistsQuery

    String idGeneratorKey = "Users.id"  // override so it doesn;t use Batch.id

    /**
     * overrides the bindAndCreate method vs events
     * as its more clear whats going on than trying to do role logic with events
     * this will already be transactional as its called from create
     */
    @Override
    void bindAndCreate(AppUser user, Map data, PersistArgs args) {
        String pwd = data['password']// data.remove('password')
        if(pwd) user.password = pwd
        bindAndSave(user, data, BindAction.Create, args)
        if(data['roles']) setUserRoles(user.id, data['roles'] as List)
    }

    /**
     * overrides the doUpdate method as its more clear whats going on than trying to do role logic with events
     */
    @Override
    AppUser doUpdate(Map data, PersistArgs args) {
        AppUser user = super.doUpdate(data, args)
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

    @RepoListener
    void beforeValidate(AppUser user, Errors errors) {
        //if its new then set defaults
        if(user.isNew()) {
            //TODO should username be set to email if thats all thats passed?
            if(!user.username) user.username = parseName(user.email)
            //name defaults
            if(!user.name) user.name = parseName(user.username)
        }
    }


    /**
     * before persist, do the password encoding
     */
    @RepoListener
    void beforePersist(AppUser user, BeforePersistEvent e) {
        if(user.isNew()) {
            //generateid, so that later it can be used for SecPasswordHistory, if required
            if(!user.id) generateId(user)
            //we check when new to avoid unique index error.
            if(exists(user.username)){
                throw DataProblemCodes.UniqueConstraint.get()
                    .detail("Violates unique constraint [username: ${user.username}]").toException()
            }
        }
        if(user.password) {
            updatePassword(user, user.password)
        }
    }

    boolean exists(String username) {
        if( !usernameExistsQuery ) usernameExistsQuery = KeyExistsQuery.of(getEntityClass()).keyName('username')
        return usernameExistsQuery.exists(username)
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
     *
     * @param user the user
     * @param roleCode the role.code, all uppercase, no ROLE_ prefix.
     * @param flushAfterPersist flush after save
     * @return the SecRoleUser that was created.
     */
    SecRoleUser addUserRole(AppUser user, String roleCode, boolean flushAfterPersist) {
        def sru = SecRoleUser.create(user, SecRole.getByCode(roleCode), flushAfterPersist)
        return sru
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

    /**
     * returns the name part of email. so jim@foo.com will return jim. if no @ then just returns whats passed in
     */
    String parseName(String name) {
        if(name.indexOf("@") != -1){
            return name.substring(0, name.indexOf("@"))
        } else {
            return name
        }
    }

    String encodePassword(String pass) {
        passwordEncoder.encode pass
    }

    /** throws EntityValidationException if not. NOTE: keep the real pas**ord name out so scanners dont pick this up */
    void isSamePass(String pass, String rePass, AppUser user) {
        if (pass.trim() != rePass.trim()) {
            throw ValidationProblem.of('password.mismatch')
                .detail("The passwords you entered do not match")
                .entity(user)
                .toException()
        }
    }

    /**
     * Sets the user roles for create or update.
     */
    @Transactional
    void setUserRoles(Long userId, List rolesId) {
        // Transform both arrays to ListArray<Long> to have ability to compare them
        List<Long> incomeRoles = rolesId.collect {
            // if the list is a map then assume its object map with and id key
            // otherwise convert value to long and assume it s list of string codes
            (it instanceof Map) ? it['id'] as Long : SecRole.getByCode((String)it).id
        }
        List<Long> existingRoles = SecRoleUser.getByUser(userId).collect{ it.role.getId() }

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
                SecRoleUser.remove(SecRole.load(id), user)
            }
        }
        if (addition?.size() > 0) {
            addition.each { Long id ->
                SecRoleUser.create(user, SecRole.load(id) as SecRole)
            }
        }
    }


    /**
     * Updates user's password, Creates password history if enabled.
     */
    void updatePassword(AppUser user, String password) {
        //no change, its same password, just exit fast
        Validate.notEmpty(password)
        if (passwordEncoder.matches(password, user.passwordHash)) return

        String hashed = encodePassword(password)

        Result valid
        if (user.isNew()) {
            //its new user, dont need to check in password history etc, just validate password
            valid = passwordValidator.validate(password)
        } else {
            //its password change for existing user, will need to check password history etc.
            valid = passwordValidator.validate(user.id, password)
        }

        if (valid.ok) {
            user.passwordHash = hashed
            user.passwordChangedDate = LocalDateTime.now()
            user.passwordExpired = false
            if (passwordConfig.historyEnabled) {
                SecPasswordHistory.repo.create(user.id, user.passwordHash)
            }
        } else {
            ValidationProblem problem = ValidationProblem.ofEntity(user)
            problem.violations(((Problem) valid).violations)
            throw problem.toException()
        }
    }

    @Transactional(readOnly = true)
    Set<String> getRoles(AppUser user) {
        List res = SecRoleUser.executeQuery('select distinct role.code from SecRoleUser where user.id = :uid', [uid: user.id] )
        return res as Set<String>
    }

    @Transactional(readOnly = true)
    Set getPermissions(AppUser user) {
        //have test
        return SecRolePermission.executeQuery(
            """
              select distinct p.permission
                    from SecRolePermission p
                    join p.role r
                    join SecRoleUser sru on sru.role = r
                    join sru.user u
                    where u.id = :uid
            """,
            [uid: user.id] ) as Set
    }

}
