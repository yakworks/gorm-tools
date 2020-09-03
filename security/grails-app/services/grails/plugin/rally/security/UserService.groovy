/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.rally.security

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.api.Criteria
import org.springframework.beans.factory.annotation.Value

import gorm.tools.security.PasswordValidator
import gorm.tools.security.domain.SecLoginHistory
import gorm.tools.security.domain.SecPasswordHistory
import gorm.tools.security.domain.SecUser
import grails.gorm.transactions.Transactional

/**
 * UserService is for user level helpers, such as sending emails to user,
 * tracking user login/logout And operations relating to passwords, contacts and org levels
 * Seurity related methods should go to SecService and not here.
 */
@CompileStatic
@Transactional
class UserService {

    @Value('${grails.plugin.rally.security.password.historyEnabled:false}')
    boolean passwordHistoryEnabled

    @Value('${grails.plugin.rally.security.password.expireDays:90}')
    int passwordExpireDays

    SecService secService
    PasswordValidator passwordValidator

    /**
     * Create new record in secLoginHistory with logged in user and date
     */
    void trackUserLogin() {
        SecUser user = secService.user
        SecLoginHistory secLoginHistory = SecLoginHistory.create([user: user, loginDate: new Date()])
    }

    @CompileDynamic //doesn't pick up maxResults with GrCompStatic
    void trackUserLogout() {
        SecUser user = secService.user
        if (!user) return
        Criteria criteria = SecLoginHistory.createCriteria()
        List secLoginHistoryList = criteria.list {
            eq("user", user)
            isNull("logoutDate")
            maxResults(1)
            order("loginDate", "desc")
        } as List
        if (secLoginHistoryList) {
            SecLoginHistory secLoginHistory = secLoginHistoryList[0]
            secLoginHistory.logoutDate = new Date()
            secLoginHistory.save()
        }
    }

    /**
     * Validate presented user passwords against config to ensure it meets password requirements.
     */
    Map validatePassword(SecUser user, String pass, String passConfirm) {
        return passwordValidator.validate(user, pass, passConfirm)
    }

    int remainingDaysForPasswordExpiry(SecUser u = null) {
        SecUser user = u ?: secService.user
        Date now = new Date()
        int expiresInDaysFromNow = TimeCategory.minus(user.passwordChangedDate + (passwordExpireDays), now).days
        return expiresInDaysFromNow
    }

    /**
     * Update user's password and creates a password history record
     * @param user
     * @param newPwd
     */
    void updatePassword(SecUser user, String newPwd) {
        user.passwd = newPwd //must be hased password
        user.mustChangePassword = false
        user.passwordChangedDate = new Date()
        user.save()

        if (passwordHistoryEnabled) {
            SecPasswordHistory.create(user, newPwd)
        }
    }

    /**
     * Check if the password exists in user's password history
     * @param user
     * @param password
     * @return
     */
    boolean passwordExistInHistory(SecUser user, String password) {
       return passwordValidator.passwordExistInHistory(user, password)
    }
}
