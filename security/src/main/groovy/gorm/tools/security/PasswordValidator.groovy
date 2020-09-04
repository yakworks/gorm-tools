/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.authentication.encoding.PasswordEncoder

import gorm.tools.security.domain.SecPasswordHistory
import gorm.tools.security.domain.SecUser
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional

@CompileStatic
class PasswordValidator {
    @Autowired
    MessageSource messageSource
    @Autowired
    PasswordEncoder passwordEncoder

    @Value('${grails.plugin.rally.security.password.minLength:4}')
    Integer passwordMinLength

    @Value('${grails.plugin.rally.security.password.mustContainNumbers:false}')
    boolean passwordMustContainNumbers

    @Value('${grails.plugin.rally.security.password.mustContainSymbols:false}')
    boolean passwordMustContainSymbols

    @Value('${grails.plugin.rally.security.password.mustContainUpperaseLetter:false}')
    boolean passwordMustContainUpperaseLetter

    @Value('${grails.plugin.rally.security.password.password.mustContainLowercaseLetter:false}')
    boolean passwordMustContainLowercaseLetter

    @Value('${grails.plugin.rally.security.password.historyEnabled:false}')
    boolean passwordHistoryEnabled

    @Value('${grails.plugin.rally.security.password.historyLength:4}')
    int passwordHistoryLength

    private String message(String key, def ...args) {
        messageSource.getMessage(key, args as Object[], key, LocaleContextHolder.locale)
    }

    @SuppressWarnings(['IfStatementCouldBeTernary'])
    Map validate(SecUser user, String pass, String passConfirm) {
        if (!pass || (pass.length() < passwordMinLength)) {
            return [ok: false, message: message("rally.security.password.minlength", passwordMinLength)]
        }

        if (passConfirm != pass) {
            return [ok: false, message: message("rally.security.password.match")]
        }

        if (passwordMustContainLowercaseLetter && !(pass =~ /^.*[a-z].*$/)) {
            return [ok: false, message: message("rally.security.password.mustcontain.lowercase")]
        }

        if (passwordMustContainUpperaseLetter && !(pass =~ /^.*[A-Z].*$/)) {
            return [ok: false, message: message("rally.security.password.mustcontain.uppercase")]
        }

        if (passwordMustContainNumbers && !(pass =~ /^.*[0-9].*$/)) {
            return [ok: false, message: message("rally.security.password.mustcontain.numbers")]
        }

        if (passwordMustContainSymbols && !(pass =~ /^.*\W.*$/)) {
            return [ok: false, message: message("rally.security.password.mustcontain.symbol")]
        }

        if (passwordHistoryEnabled && passwordExistInHistory(user, pass)) {
            return [ok: false, message: message("rally.security.password.existsinhistory", passwordHistoryLength)]
        }

        return [ok: true]
    }

    /**
     * Check if the password exists in user's password history
     */
    @GrailsCompileStatic
    @Transactional(readOnly = true)
    boolean passwordExistInHistory(SecUser user, String password) {
        List<SecPasswordHistory> passwordHistoryList = SecPasswordHistory.findAllByUser(user)
        passwordHistoryList.any { passwordEncoder.isPasswordValid(it.password, password, null) }
    }

}
