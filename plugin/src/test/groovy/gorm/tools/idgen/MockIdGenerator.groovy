package gorm.tools.idgen

import grails.gorm.transactions.Transactional
import org.springframework.transaction.annotation.Propagation

/**
 *  This is mocked out like so
 *  KeyName    |OID
 *  -----------|--------
 *  table.id   |1
 *  table1.id  |99
 *
 *  table3.id does not exist
 */
class MockIdGenerator implements IdGenerator {

    private long seedValue = 1000 //the Id to start with if it does not exist in the table
    public Map<String, Integer> table = new HashMap<String, Integer>()

    MockIdGenerator() {
        table.put("table.id", 1)
        table.put("table1.id", 99)
    }

    @Transactional
    long getNextId(String keyName, long increment) {
        return internalGetNextId(keyName, increment)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private long internalGetNextId(String keyName, long increment) {
        long oid = 0
        if (table.containsKey(keyName)) {
            oid = table.get(keyName)
            table.put(keyName, oid + increment)
        }
        if (oid == 0) { //no rows exist so create it
            oid = seedValue
            long futureId = seedValue + increment
            table.put(keyName, futureId)
        }
        return oid
    }

    @Transactional
    long getNextId(String keyName) {
        return internalGetNextId(keyName, 1)
    }
}
