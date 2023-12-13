/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm

import java.time.Duration
import java.time.LocalDateTime

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.api.Criteria
import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Transactional
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecLoginHistory
import yakworks.security.gorm.model.SecPasswordHistory
import yakworks.security.services.PasswordValidator
import yakworks.security.user.CurrentUser

/**
 * AppUserService is for user level helpers, such as sending emails to user,
 * tracking user username/logout And operations relating to passwords, contacts and org levels
 * Seurity related methods should go to SecService and not here.
 */
@CompileStatic
@Transactional
class AppUserService {

    @Autowired
    CurrentUser currentUser

    @Autowired(required = false) //required = false so in case spring sec is not working
    PasswordValidator passwordValidator

    /**
     * Create new record in secLoginHistory with logged in user and date
     */
    void trackUserLogin() {
        Long userId = currentUser.userId as Long
        SecLoginHistory secLoginHistory = SecLoginHistory.create([userId: userId, loginDate: new Date()])
    }

    @CompileDynamic //doesn't pick up maxResults with GrCompStatic
    void trackUserLogout() {
        Long userId = currentUser.userId as Long
        if (!userId) return
        Criteria criteria = SecLoginHistory.createCriteria()
        List secLoginHistoryList = criteria.list {
            eq("userId", userId)
            isNull("logoutDate")
            maxResults(1)
            order("loginDate", "desc")
        } as List
        if (secLoginHistoryList) {
            SecLoginHistory secLoginHistory = secLoginHistoryList[0]
            secLoginHistory.logoutDate = LocalDateTime.now()
            secLoginHistory.save()
        }
    }

    Integer remainingDaysForPasswordExpiry(AppUser u) {
        LocalDateTime pExpire = u.passwordChangedDate.plusDays(passwordValidator.passwordExpireDays)
        return Duration.between(LocalDateTime.now(), pExpire).toDays().toInteger()
    }

    /**
     * Update user's password and creates a password history record
     */
    void updatePassword(AppUser user, String newPwd) {
        user.password = newPwd //must be hased password
        user.passwordExpired = false
        user.passwordChangedDate = LocalDateTime.now()
        user.save()

        if (passwordValidator.passwordHistoryEnabled) {
            SecPasswordHistory.create(user, newPwd)
        }
    }

    /**
     * Check if the password exists in user's password history
     */
    boolean passwordExistInHistory(AppUser user, String password) {
        return passwordValidator.passwordExistInHistory(user, password)
    }
}
