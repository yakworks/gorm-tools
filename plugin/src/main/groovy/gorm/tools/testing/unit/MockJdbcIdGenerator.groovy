package gorm.tools.testing.unit

import gorm.tools.idgen.IdGenerator
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.springframework.transaction.annotation.Propagation

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@SuppressWarnings('SynchronizedMethod')
@CompileStatic
class MockJdbcIdGenerator implements IdGenerator {

    static long seedValue = 1 //the Id to start with if it does not exist in the table
    ConcurrentMap<String, Long> table = new ConcurrentHashMap<String, Long>()
    //Map<String, Integer> table = new HashMap<String, Integer>()

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    synchronized long getNextId(String keyName, long increment) {
        return updateIncrement(keyName, increment)
    }

    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    private long updateIncrement(String keyName, long increment) {
        long oid
        if (table.containsKey(keyName)) {
            oid = table.get(keyName)
            table.put(keyName, oid + increment)
        } else { //no rows exist so create it
            oid = seedValue
            long futureId = seedValue + increment
            table.put(keyName, futureId)
        }
        return oid
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    synchronized long getNextId(String keyName) {
        return updateIncrement(keyName, 1)
    }
}
