/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import java.time.LocalDateTime

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class SecPasswordHistory {
    static belongsTo = [user: AppUser]
    String password

    LocalDateTime dateCreated

    static constraints = {
        user nullable: false
        password nullable: false
        dateCreated nullable: true
    }

    static mapping = {
        version false
        user column: "userId"
        password updateable: false
        user updateable: false
        dateCreated column: "dateCreated"
    }

    /**
     * Creates new password history for given user. Will remove the oldest entry if the number of history records goes more thn MAX_HISTORY
     */
    static SecPasswordHistory create(AppUser user, String passwordHash) {
        Integer historyLength = 10//AppParam.value('passwordHistoryLength').toInteger()
        if (SecPasswordHistory.countByUser(user) >= historyLength) {
            SecPasswordHistory lastRecord = SecPasswordHistory.list(max: 1, sort: 'dateCreated', order: 'asc')[0]
            lastRecord.delete()
        }
        SecPasswordHistory passwordHistory = new SecPasswordHistory(user: user, password: passwordHash)
        passwordHistory.save(flush: false)
        return passwordHistory
    }

    static List<SecPasswordHistory> findAllByUser(AppUser user){
        SecPasswordHistory.findAllByUser(user)
    }
}
