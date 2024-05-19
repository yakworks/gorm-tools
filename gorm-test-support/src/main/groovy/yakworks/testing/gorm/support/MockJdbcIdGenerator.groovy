/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.support

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import groovy.transform.CompileStatic

import org.springframework.transaction.annotation.Propagation

import gorm.tools.idgen.IdGenerator
import grails.gorm.transactions.Transactional

/**
 * testing stub for the IdGenerator
 */
@SuppressWarnings('SynchronizedMethod')
@CompileStatic
class MockJdbcIdGenerator implements IdGenerator {

    static long seedValue = 1000 //the Id to start with if it does not exist in the table
    ConcurrentMap<String, Long> hashTable = new ConcurrentHashMap<String, Long>()
    //Map<String, Integer> table = new HashMap<String, Integer>()

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    synchronized long getNextId(String keyName, long increment) {
        return updateIncrement(keyName, increment)
    }

    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    private long updateIncrement(String keyName, long increment) {
        long oid
        if (hashTable.containsKey(keyName)) {
            oid = hashTable.get(keyName)
            hashTable.put(keyName, oid + increment)
        } else { //no rows exist so create it
            oid = seedValue
            long futureId = seedValue + increment
            hashTable.put(keyName, futureId)
        }
        return oid
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    synchronized long getNextId(String keyName) {
        return updateIncrement(keyName, 1)
    }
}
