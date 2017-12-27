package gorm.tools.idgen

import groovy.transform.CompileStatic
import org.apache.commons.lang.Validate
import org.apache.log4j.Category

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

/**
 * An Thread safe implementation that caches a range of values in memory by the key name (ie: "tablename.id")
 * Default cache allocation size is 50 but can be set to other values. requires another IdGenerator implementation to be
 * set in the constructor. Usually a Jdbc type implmentation that will get the values for this class for both initialization
 * and when this runs out of allocated ids.
 *
 * @author Joshua Burnett (@basejump)
 * @since 1.0
 *
 */
@CompileStatic
class BatchIdGenerator implements IdGenerator {
    private static Category log = Category.getInstance(BatchIdGenerator.class)

    //holds the rows of the id generation by key.
    private ConcurrentMap<String, AtomicReference<IdTuple>> idTupleMap = new ConcurrentHashMap<String, AtomicReference<IdTuple>>()
    //overrides default for the keys
    private ConcurrentMap<String, Long> batchSizeByKey = new ConcurrentHashMap<String, Long>()

    private IdGenerator generator
    Long defaultBatchSize = 100000

    AtomicLong dummy = new AtomicLong(1000)

    BatchIdGenerator() { }

    BatchIdGenerator(IdGenerator generator) {
        setGenerator(generator)
    }

    /**
     * override the default batchSize for the specified key
     * @param keyName
     * @param batchSize
     */
    void setBatchSize(String keyName, Long batchSize) {
        batchSizeByKey[keyName] = batchSize
    }
    long getBatchSize(String keyName) {
        if (batchSizeByKey.containsKey(keyName)) {
            return batchSizeByKey[keyName]
        }
        return defaultBatchSize
    }

    long getNextId(String keyName) {
        dummy.getAndIncrement()
//        AtomicReference<IdTuple> tuple = findOrCreate(keyName)
//        getAndUpdate(keyName, tuple)
    }

    /**
     * this implementation does nothing here. throws IllegalAccessException
     */
    long getNextId(String keyName, long batchSize) {
        throw new IllegalAccessException("Use reserveIds get a specific icnrement size or setBatchSize(keyname,size) to set by key ")
    }

    /**
     * Uses a lambda/closure to go get a new set of ids if the current iterator is at its max
     * @return the id to use
     */
    long getAndUpdate(String keyName, AtomicReference<IdTuple> idAtomic) {

        // the return value here in the closure is what is stored in the atomic for use next time through, not now.
        // "current" tuple here is whats actually returned in the getAndUpdate itself
        UnaryOperator updateOp = { IdTuple current ->
            if(current.atMax){
                IdTuple newTuple
                synchronized (keyName.intern()) {
                    long batchSize = getBatchSize(keyName)
                    long nextId = getGenerator().getNextId(keyName, batchSize)
                    newTuple = new IdTuple(nextId, nextId + batchSize - 1)
                }
                return newTuple
            }
            return new IdTuple(current.nextId + 1 , current.maxId)
        } as UnaryOperator<IdTuple>

        return idAtomic.getAndUpdate(updateOp).nextId
    }

    AtomicReference<IdTuple> findOrCreate(String keyName) {
        Validate.notNull(keyName, "The row key name can't be null")

        if (!idTupleMap.containsKey(keyName)) {
            //synchronize on the keyname and just let 1 thread create them. Creation should only happen once
            // so no bottlenecks should occure.
            // Note: itern forces it to use same string in memory. see http://java-performance.info/string-intern-java-6-7-8-multithreaded-access/
            synchronized (keyName.intern()) {
                log.debug("Creating a BatchIDGenerator.IdTuple for " + keyName)

                //check to see if the size is overriden for the key, otherwise go with the default size
                long batchSize = (batchSizeByKey.containsKey(keyName)) ? batchSizeByKey[keyName] : defaultBatchSize

                //go to the (jdbcBatchIdGen) and get a new batch range.
                long current = getGenerator().getNextId(keyName, batchSize)

                idTupleMap.put(
                    keyName,
                    new AtomicReference<IdTuple>(new IdTuple(current, current + batchSize - 1))
                )
            }

        }
        return idTupleMap.get(keyName)
    }

    /**
     * Used before creating large batches where you know the count and want to pre pool and id range larger than the default
     * to avoid the DB round trips to keep incrementing the sequence in the table.
     * YOU DO NOT WANT to use this repeatedly.
     * In this implementation this forces a call to the injected primary idGenerator (jdbcBatchIdGen?)
     * with the specified batchSize and erases/looses forever the ids currently in memory.
     * This is fine if you don't care about a possible gap in the ids.
     */
//    void reserveIds(String keyName, long batchSize) {
//        synchronized (keyName.intern()) {
//            log.debug("Creating a BatchIDGenerator.IdTuple for " + keyName)
//
//            idTupleMap.put(keyName,
//                new AtomicReference<IdTuple>(
//                    new IdTuple(
//                        keyName,
//                        getGenerator().getNextId(keyName, batchSize),
//                        batchSize)
//                )
//            )
//        }
//    }

    // MARKER holder class for cached id info
    class IdTuple {
        final long maxId //if nextID reaches this point it time to hit the db(generator) for a new set of values
        final boolean atMax
        final long nextId

        IdTuple(long currentId, long maxId) {
            this.nextId = currentId //new AtomicLong(currentId)
            this.maxId = maxId
            if(nextId == maxId) atMax = true
            if(nextId > maxId) throw new IllegalStateException("ID can't be greater than maxId")
        }

//        long getAndIncrement() {
//            long id = nextId.getAndIncrement()
//            if(id > maxId) throw new IllegalStateException("ID can't be greater than maxId")
//            return id
//        }
    }

    void setGenerator(IdGenerator generator) {
        Validate.isTrue(this.generator == null, "IdGenerator is already created, no hot swapping")
        this.generator = generator
    }

    private IdGenerator getGenerator() {
        return generator
    }

}
