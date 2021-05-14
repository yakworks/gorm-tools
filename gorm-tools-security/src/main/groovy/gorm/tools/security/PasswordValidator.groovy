/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.crypto.password.PasswordEncoder

import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecPasswordHistory
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional

@CompileStatic
class PasswordValidator {

    @Inject MessageSource messageSource
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

    private String message(String key, def ...args) {
        messageSource.getMessage(key, args as Object[], key, LocaleContextHolder.locale)
    }

    @SuppressWarnings(['IfStatementCouldBeTernary'])
    Map validate(AppUser user, String pass, String passConfirm) {
        if (!pass || (pass.length() < passwordMinLength)) {
            return [ok: false, message: message("gorm.tools.security.password.minlength", passwordMinLength)]
        }

        if (passConfirm != pass) {
            return [ok: false, message: message("gorm.tools.security.password.match")]
        }

        if (passwordMustContainLowercaseLetter && !(pass =~ /^.*[a-z].*$/)) {
            return [ok: false, message: message("gorm.tools.security.password.mustcontain.lowercase")]
        }

        if (passwordMustContainUpperaseLetter && !(pass =~ /^.*[A-Z].*$/)) {
            return [ok: false, message: message("gorm.tools.security.password.mustcontain.uppercase")]
        }

        if (passwordMustContainNumbers && !(pass =~ /^.*[0-9].*$/)) {
            return [ok: false, message: message("gorm.tools.security.password.mustcontain.numbers")]
        }

        if (passwordMustContainSymbols && !(pass =~ /^.*\W.*$/)) {
            return [ok: false, message: message("gorm.tools.security.password.mustcontain.symbol")]
        }

        if (passwordHistoryEnabled && passwordExistInHistory(user, pass)) {
            return [ok: false, message: message("gorm.tools.security.password.existsinhistory", passwordHistoryLength)]
        }

        return [ok: true]
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
