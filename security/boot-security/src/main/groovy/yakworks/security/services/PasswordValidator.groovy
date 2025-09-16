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
import yakworks.api.problem.ViolationFieldError
import yakworks.message.Msg
import yakworks.message.MsgKey
import yakworks.security.PasswordConfig

@CompileStatic
class PasswordValidator {

    @Inject PasswordEncoder passwordEncoder
    @Inject PasswordConfig passwordConfig

    /*
    security.validation.password.error=password validation failed
security.validation.password.existsinhistory=Password must be different from the last {value} passwords

     */
    @SuppressWarnings(['IfStatementCouldBeTernary'])
    Result validate(String pass) {
        List problemKeys = [] as List<MsgKey>
        if (!pass || (pass.length() < passwordConfig.minLength)) {

            problemKeys.add(
                Msg.key("security.validation.password.minlength", [min: passwordConfig.minLength])
                    .fallbackMessage("Password must be minimum ${passwordConfig.minLength} characters long")
            )
        }

        if (passwordConfig.mustContainLowercaseLetter && !(pass =~ /^.*[a-z].*$/)) {
            problemKeys.add(
                Msg.key("security.validation.password.mustcontain.lowercase")
                    .fallbackMessage("Password must contain a lower case letter")
            )
        }

        if (passwordConfig.mustContainUppercaseLetter && !(pass =~ /^.*[A-Z].*$/)) {
            problemKeys.add(
                Msg.key("security.validation.password.mustcontain.uppercase")
                    .fallbackMessage("Password must contain a uppercase letter")
            )
        }

        if (passwordConfig.mustContainNumbers && !(pass =~ /^.*[0-9].*$/)) {
            problemKeys.add(
                Msg.key("security.validation.password.mustcontain.numbers")
                    .fallbackMessage("Password must contain a number")
            )
        }

        if (passwordConfig.mustContainSymbols && !(pass =~ /^.*\W.*$/)) {
            problemKeys.add(
                Msg.key("security.validation.password.mustcontain.symbol")
                    .fallbackMessage("Password must contain a symbol")
            )
        }

        if(problemKeys){
            var vs = problemKeys.collect { ViolationFieldError.of(it).field('password')  }
            var prob = Problem.of('security.validation.password.error')
            prob.violations(vs)
            return prob
        } else {
            return  Result.OK()
        }
    }

    /**
     * For user specific password validation, which can incorporate user's password history etc.
     * The defalt implementation just validates the password. Subclasses can override to hook up passwordhistory etc
     */
    Result validate(Serializable userId, String pass) {
        return validate(pass)
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
