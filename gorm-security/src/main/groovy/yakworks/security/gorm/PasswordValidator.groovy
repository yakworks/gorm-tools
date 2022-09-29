/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm

import java.time.LocalDate
import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.message.Msg
import yakworks.message.MsgKey
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecPasswordHistory

@CompileStatic
class PasswordValidator {

    @Inject PasswordEncoder passwordEncoder

    @Value('${yakworks.security.password.expireDays:90}')
    int passwordExpireDays

    @Value('${yakworks.security.password.expireEnabled:false}')
    boolean passwordExpiryEnabled

    @Value('${yakworks.security.password.warnDays:30}')
    int passwordWarnDays

    @Value('${yakworks.security.password.minLength:4}')
    Integer passwordMinLength

    @Value('${yakworks.security.password.mustContainNumbers:false}')
    boolean passwordMustContainNumbers

    @Value('${yakworks.security.password.mustContainSymbols:false}')
    boolean passwordMustContainSymbols

    @Value('${yakworks.security.password.mustContainUpperaseLetter:false}')
    boolean passwordMustContainUpperaseLetter

    @Value('${yakworks.security.password.password.mustContainLowercaseLetter:false}')
    boolean passwordMustContainLowercaseLetter

    @Value('${yakworks.security.password.historyEnabled:false}')
    boolean passwordHistoryEnabled

    @Value('${yakworks.security.password.historyLength:4}')
    int passwordHistoryLength

    @SuppressWarnings(['IfStatementCouldBeTernary'])
    Result validate(AppUser user, String pass, String passConfirm) {
        List problemKeys = [] as List<MsgKey>
        if (!pass || (pass.length() < passwordMinLength)) {
            problemKeys << Msg.key("security.validation.password.minlength", [min: passwordMinLength])
        }

        if (passConfirm != pass) {
            problemKeys << Msg.key("security.validation.password.match")
        }

        if (passwordMustContainLowercaseLetter && !(pass =~ /^.*[a-z].*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.lowercase")
        }

        if (passwordMustContainUpperaseLetter && !(pass =~ /^.*[A-Z].*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.uppercase")
        }

        if (passwordMustContainNumbers && !(pass =~ /^.*[0-9].*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.numbers")
        }

        if (passwordMustContainSymbols && !(pass =~ /^.*\W.*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.symbol")
        }

        if (passwordHistoryEnabled && passwordExistInHistory(user, pass)) {
            problemKeys << Msg.key("security.validation.password.minlength", [value: passwordHistoryLength])
        }

        if(problemKeys){
            return Problem.of('security.validation.password.error').addViolations(problemKeys)
        } else {
            return  Result.OK()
        }
    }

    /**
     * Check if the password exists in user's password history
     */
    @GrailsCompileStatic
    @Transactional(readOnly = true)
    boolean passwordExistInHistory(AppUser user, String password) {
        List<SecPasswordHistory> passwordHistoryList = SecPasswordHistory.findAllByUser(user)
        passwordHistoryList.any { passwordEncoder.matches(it.password, password) }
    }

    /**
     * checks if password is expired. first checks the passwordExpired field and then if expireEnabled
     * it adds the expireDays to see if we are under that date
     * @param user is optional, will look in the security context if not passed in
     */
    boolean isPasswordExpired(AppUser user) {
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
