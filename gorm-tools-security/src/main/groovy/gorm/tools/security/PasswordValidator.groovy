/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder

import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecPasswordHistory
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.message.Msg
import yakworks.message.MsgKey

@CompileStatic
class PasswordValidator {

    @Inject PasswordEncoder passwordEncoder

    @Value('${gorm.tools.security.password.minLength:4}')
    Integer passwordMinLength

    @Value('${gorm.tools.security.password.mustContainNumbers:false}')
    boolean passwordMustContainNumbers

    @Value('${gorm.tools.security.password.mustContainSymbols:false}')
    boolean passwordMustContainSymbols

    @Value('${gorm.tools.security.password.mustContainUpperaseLetter:false}')
    boolean passwordMustContainUpperaseLetter

    @Value('${gorm.tools.security.password.password.mustContainLowercaseLetter:false}')
    boolean passwordMustContainLowercaseLetter

    @Value('${gorm.tools.security.password.historyEnabled:false}')
    boolean passwordHistoryEnabled

    @Value('${gorm.tools.security.password.historyLength:4}')
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

}
