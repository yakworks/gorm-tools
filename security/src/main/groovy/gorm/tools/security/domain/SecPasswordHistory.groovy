/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class SecPasswordHistory {
    static belongsTo = [user: SecUser]
    String password

    Date dateCreated

    static constraints = {
        user nullable: false
        password nullable: false, blank: false
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
     *
     * @param user
     * @param passwordHash
     * @return
     */
    static SecPasswordHistory create(SecUser user, String passwordHash) {
        Integer historyLength = 10//AppParam.value('passwordHistoryLength').toInteger()
        if (SecPasswordHistory.countByUser(user) >= historyLength) {
            SecPasswordHistory lastRecord = SecPasswordHistory.list(max: 1, sort: 'dateCreated', order: 'asc')[0]
            lastRecord.delete()
        }
        SecPasswordHistory passwordHistory = new SecPasswordHistory(user: user, password: passwordHash)
        passwordHistory.save(flush: false)
        return passwordHistory
    }
}
