/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.api.Criteria
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecLoginHistory
import yakworks.security.gorm.model.SecPasswordHistory
import yakworks.security.user.CurrentUser

/**
 * AppUserService is for user level helpers, such as sending emails to user,
 * tracking user username/logout And operations relating to passwords, contacts and org levels
 * Seurity related methods should go to SecService and not here.
 */
@CompileStatic
@Transactional
class AppUserService {

    @Value('${yakworks.security.password.historyEnabled:false}')
    boolean passwordHistoryEnabled

    @Value('${yakworks.security.password.expireDays:90}')
    int passwordExpireDays

    @Value('${yakworks.security.password.expireEnabled:false}')
    boolean passwordExpiryEnabled

    @Value('${yakworks.security.password.warnDays:30}')
    int passwordWarnDays

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
            secLoginHistory.logoutDate = new Date()
            secLoginHistory.save()
        }
    }

    /**
     * Validate presented user passwords against config to ensure it meets password requirements.
     */
    Result validatePassword(AppUser user, String pass, String passConfirm) {
        return passwordValidator.validate(user, pass, passConfirm)
    }

    /**
     * checks if password is expired. first checks the passwordExpired field and then if expireEnabled
     * it adds the expireDays to see if we are under that date
     * @param user is optional, will look in the security context if not passed in
     */
    //FIXME make sure we have good integration tests for this
    boolean isPasswordExpired(AppUser user = null) {
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

    Integer remainingDaysForPasswordExpiry(AppUser u) {
        LocalDateTime pExpire = u.passwordChangedDate.plusDays(passwordExpireDays)
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

        if (passwordHistoryEnabled) {
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
