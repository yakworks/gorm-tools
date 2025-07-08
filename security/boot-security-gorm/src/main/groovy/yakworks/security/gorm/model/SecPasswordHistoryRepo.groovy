/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import java.time.LocalDateTime
import javax.inject.Inject
import javax.transaction.Transactional

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.LongIdGormRepo
import yakworks.api.problem.data.DataProblem
import yakworks.security.PasswordConfig

@Slf4j
@CompileStatic
@GormRepository
class SecPasswordHistoryRepo extends LongIdGormRepo<SecPasswordHistory> {
    @Inject PasswordConfig passwordConfig

    /**
     *
     * Creates password history for user,
     * Will keep only `passwordConfig.historyLength` records and deletes older if there are more
     */
    @Transactional
    SecPasswordHistory create(Serializable uid, String pwdHash) {
        if(!passwordConfig.historyEnabled) {
            throw DataProblem.ex("Password history is not enabled")
        }
        if (SecPasswordHistory.query(userId:uid).count() >= passwordConfig.historyLength) {
            SecPasswordHistory lastRecord = SecPasswordHistory.query(userId:uid, sort: 'dateCreated', order: 'asc').get()
            lastRecord.delete()
        }
        SecPasswordHistory passwordHistory = new SecPasswordHistory(userId: uid as Long, password: pwdHash, dateCreated: LocalDateTime.now())
        passwordHistory.persist(PersistArgs.of(insert: true))
    }

    Collection<SecPasswordHistory> findAllByUser(Serializable uid) {
        return SecPasswordHistory.query(userId:uid).list()
    }


    @Override
    SecPasswordHistory update(Map data, PersistArgs args) {
        throw new UnsupportedOperationException("Updating SecPasswordHistory is not allowed")
    }

    @Override
    void removeById(Serializable id) {
        throw new UnsupportedOperationException("Deleting SecPasswordHistory is not allowed")
    }
}
