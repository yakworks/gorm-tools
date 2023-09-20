/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm

import java.time.LocalDate

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.message.Msg
import yakworks.message.MsgKey
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecPasswordHistory
import yakworks.security.services.PasswordValidator

/**
 * Older password validation for ApUser and SecPasswordHistory.
 */
@CompileStatic
class AppUserPasswordValidator extends PasswordValidator {

    @Override
    Result validate(String pass, String passConfirm) {
        def res = super.validate(pass, passConfirm)
        if(!res.ok) return res

        List problemKeys = [] as List<MsgKey>

        // TODO
        // if (passwordHistoryEnabled && passwordExistInHistory(user, pass)) {
        //     problemKeys << Msg.key("security.validation.password.minlength", [value: passwordHistoryLength])
        // }

        if(problemKeys){
            return Problem.of('security.validation.password.error').addViolations(problemKeys)
        } else {
            return  Result.OK()
        }
    }

    /**
     * Check if the password exists in user's password history
     * NOT USED RIGHT NOW, KEPT FOR REF
     */
    @Override
    @CompileDynamic
    @Transactional(readOnly = true)
    boolean passwordExistInHistory(Serializable id, String password) {
        AppUser user = AppUser.get(id)
        List<SecPasswordHistory> passwordHistoryList = SecPasswordHistory.findAllByUser(user)
        passwordHistoryList.any { passwordEncoder.matches(it.password, password) }
    }

    /**
     * checks if password is expired. first checks the passwordExpired field and then if expireEnabled
     * it adds the expireDays to see if we are under that date
     * @param user is optional, will look in the security context if not passed in
     */
    @Override
    boolean isPasswordExpired(Serializable id) {
        AppUser user = AppUser.get(id)
        //can always force a password change by setting passwordExpired field to true
        if(user.passwordExpired) return true
        if (passwordExpiryEnabled) {
            LocalDate expireDate = user.passwordChangedDate?.plusDays(passwordExpireDays).toLocalDate()
            //check if user's password has expired
            if (!expireDate || LocalDate.now() >= expireDate) {
                return true
            }
        }
        return false
    }
}
