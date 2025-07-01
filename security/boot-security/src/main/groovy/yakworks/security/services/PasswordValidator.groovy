/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.services

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.security.crypto.password.PasswordEncoder

import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.message.Msg
import yakworks.message.MsgKey
import yakworks.security.PasswordConfig

@CompileStatic
class PasswordValidator {

    @Inject PasswordEncoder passwordEncoder
    @Inject PasswordConfig passwordConfig

    @SuppressWarnings(['IfStatementCouldBeTernary'])
    Result validate(String pass, String passConfirm) {
        List problemKeys = [] as List<MsgKey>
        if (!pass || (pass.length() < passwordConfig.minLength)) {
            problemKeys << Msg.key("security.validation.password.minlength", [min: passwordConfig.minLength])
        }

        if (passConfirm != pass) {
            problemKeys << Msg.key("security.validation.password.match")
        }

        if (passwordConfig.mustContainLowercaseLetter && !(pass =~ /^.*[a-z].*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.lowercase")
        }

        if (passwordConfig.mustContainUppercaseLetter && !(pass =~ /^.*[A-Z].*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.uppercase")
        }

        if (passwordConfig.mustContainNumbers && !(pass =~ /^.*[0-9].*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.numbers")
        }

        if (passwordConfig.mustContainSymbols && !(pass =~ /^.*\W.*$/)) {
            problemKeys << Msg.key("security.validation.password.mustcontain.symbol")
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
    boolean passwordExistInHistory(Serializable userId, String password) {
        throw new UnsupportedOperationException("Not Implemented")
    }

    /**
     * checks if password is expired. first checks the passwordExpired field and then if expireEnabled
     * it adds the expireDays to see if we are under that date
     * @param user is optional, will look in the security context if not passed in
     */
    boolean isPasswordExpired(Serializable id) {
        return false
    }
}
