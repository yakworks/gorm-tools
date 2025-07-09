/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileStatic

import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.message.Msg
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecPasswordHistory
import yakworks.security.services.PasswordValidator

/**
 * Password validation for ApUser and SecPasswordHistory.
 */
@CompileStatic
class AppUserPasswordValidator extends PasswordValidator {

    @Override
    Result validate(Serializable userId, String pass) {
        def res = super.validate(pass)
        if (!res.ok) return res //return fast if ok

        if (passwordExistInHistory(userId, pass)) {
            var msgKey= Msg.key("security.validation.password.existsinhistory", [value: passwordConfig.historyLength])
            return Problem.of('security.validation.password.error').addViolations([msgKey])
        }
        //return ok if its all good
        return res
    }

    /**
     * Check if the password exists in user's password history
     *
     * @param id AppUser.id
     * @param password what to check
     * @return true if it exists, false if not enabled or does not exist
     */
    @Override
    @Transactional(readOnly = true)
    boolean passwordExistInHistory(Serializable id, String password) {
        if (passwordConfig.historyEnabled) {
            List<SecPasswordHistory> passwordHistoryList = SecPasswordHistory.query(userId: id).list()
            return passwordHistoryList.any { passwordEncoder.matches(password, it.password) }
        }
        return false
    }

    /**
     * checks if password is expired. first checks the passwordExpired field and then if expireEnabled
     * it adds the expireDays to see if we are under that date
     *
     * @param user is optional, will look in the security context if not passed in
     */
    @Override
    boolean isPasswordExpired(Serializable id) {
        AppUser user = AppUser.get(id)

        //if user without password
        if(user.passwordHash == null) return false

        //can always force a password change by setting passwordExpired field to true
        if (user.passwordExpired) return true

        if (passwordConfig.expiryEnabled) {

            //ideally all user should have passwordChangedDate, it gets set initially when user is created and keeps getting updated
            //but if null for some old users, thn expire it.
            if(user.passwordChangedDate == null) return true

            //see if lastChangeDate + passwordExpireDays < today - thn password has already expired
            LocalDateTime expiresOnDate = user.passwordChangedDate.plusDays(passwordConfig.passwordExpireDays)
            if (expiresOnDate.toLocalDate() < LocalDate.now()) {
                return true
            }
        }
        return false
    }
}
