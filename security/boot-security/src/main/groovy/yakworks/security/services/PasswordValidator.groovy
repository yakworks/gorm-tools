/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.services

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.security.crypto.password.PasswordEncoder

import yakworks.api.Result
import yakworks.api.problem.GenericProblem
import yakworks.api.problem.Problem
import yakworks.api.problem.ViolationFieldError
import yakworks.message.Msg
import yakworks.message.MsgKey
import yakworks.message.MsgServiceRegistry
import yakworks.message.spi.MsgService
import yakworks.security.PasswordConfig

@CompileStatic
class PasswordValidator {

    @Inject PasswordEncoder passwordEncoder
    @Inject PasswordConfig passwordConfig

    @SuppressWarnings(['IfStatementCouldBeTernary'])
    Result validate(String pass) {
        List problemKeys = [] as List<MsgKey>
        if (!pass || (pass.length() < passwordConfig.minLength)) {
            problemKeys << Msg.key("security.validation.password.minlength", [min: passwordConfig.minLength])
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
            //Problem.of('security.validation.password.error').addViolations(problemKeys)
            return addViolations(Problem.of('security.validation.password.error'), problemKeys)
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

    /**
     * Need to set code/msg explicitely on ViolationFieldError if we are adding violations
     * For regular field errors, ProblemHandler does this by calling ValidationProblem.transateErrorsToViolations
     */
    protected Problem addViolations(Problem p, List<MsgKey> errs) {
        MsgService msgService = MsgServiceRegistry.service
        for(MsgKey k : errs) {
            String msg = msgService.get(k)
            p.violations.add(ViolationFieldError.of(k.code, msg))
        }
        return p
    }

}
