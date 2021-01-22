/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.idgen

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import yakworks.commons.lang.Validate

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
@Slf4j
class PooledIdGenerator implements IdGenerator {

    //holds the rows of the id generation by key.
    private final ConcurrentMap<String, AtomicReference<IdTuple>> idTupleMap = new ConcurrentHashMap<String, AtomicReference<IdTuple>>()
    //overrides default for the keys
    private final ConcurrentMap<String, Long> batchSizeByKey = new ConcurrentHashMap<String, Long>()
    //overrides all defaults for a single use next time fetching occurs with the injected generator
    //private ConcurrentMap<String, Long> batchSizeByKeyUseOnce = new ConcurrentHashMap<String, Long>()

    private IdGenerator generator

    Long defaultBatchSize = 255

    //private final AtomicLong dummy = new AtomicLong(1000)

    PooledIdGenerator() { }

    PooledIdGenerator(IdGenerator generator) {
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
        //dummy.getAndIncrement()
        AtomicReference<IdTuple> tuple = findOrCreate(keyName)
        return getAndUpdate(keyName, tuple)
    }

    /**
     * this implementation does nothing here. throws IllegalAccessException
     */
    long getNextId(String keyName, long batchSize) {
        throw new IllegalAccessException("Use reserveIds get a specific icrement size or setBatchSize(keyname,size) to set by key ")
    }

    /**
     * Uses a lambda/closure to go get a new set of ids if the current iterator is at its max
     * @return the id to use
     */
    long getAndUpdate(String keyName, AtomicReference<IdTuple> idAtomic) {

        IdTuple currentTup = idAtomic.get()
        long nextId = currentTup.getAndIncrement()

        //if nextId is 0 then its equal or over the maxId and needs a refresh
        while(nextId == 0) {
            //synchronize on the key so only 1 thread can get a new set of ids at a time for a keyName
            synchronized (keyName.intern()) {
                /* only do it if the atomic tuple is the original one we got to ensure
                another thread following right behind doesn't hit the db again */
                if (idAtomic.get() == currentTup) {
                    //check again now that we are inside of synchronized to make sure it didn't already get run
                    long batchSize = getBatchSize(keyName)
                    long futureId = getGenerator().getNextId(keyName, batchSize)
                    assert idAtomic.compareAndSet(currentTup, new IdTuple(futureId, batchSize))
                }
                nextId = idAtomic.get().getAndIncrement()
            }
        }
        return nextId
    }

    AtomicReference<IdTuple> findOrCreate(String keyName) {
        Validate.notEmpty(keyName, "keyName arg")

        if (!idTupleMap.containsKey(keyName)) {
            // synchronize on the keyname and just let 1 thread create them. Creation should only happen once
            // so no bottlenecks should occur.
            // Note: itern forces it to use same string in memory. see http://java-performance.info/string-intern-java-6-7-8-multithreaded-access/
            synchronized (keyName.intern()) {
                //double check again and exit if another thread was here already and created it
                if (idTupleMap.containsKey(keyName)) return idTupleMap.get(keyName)

                // log.debug("Creating a BatchIDGenerator.IdTuple for " + keyName)

                //check to see if the size is overriden for the key, otherwise go with the default size
                long batchSize = getBatchSize(keyName) //(batchSizeByKey.containsKey(keyName)) ? batchSizeByKey[keyName] : defaultBatchSize

                //go to the (jdbcBatchIdGen) and get a new batch range.
                long current = getGenerator().getNextId(keyName, batchSize)

                idTupleMap.put(
                    keyName,
                    new AtomicReference<IdTuple>(new IdTuple(current, batchSize))
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
     * with the specified batchSize and erases/looses forever the ids that are currently in memory.
     * This is fine if you don't care about a possible gap in the ids.
     */
//    void reserveIds(String keyName, long batchSize) {
//    }

    // MARKER holder class for cached id info
    @CompileStatic
    class IdTuple {
        private final long maxId //if nextID reaches this point it time to hit the db(generator) for a new set of values
        private final AtomicLong nextId

        IdTuple(long currentId, long batchSize) {
            this.nextId = new AtomicLong(currentId) //new AtomicLong(currentId)
            this.maxId = currentId + batchSize
            if(currentId >= maxId) throw new IllegalStateException("ID can't be greater than maxId")
        }

        long getAndIncrement() {
            long id = nextId.getAndIncrement()
            if(id >= maxId) return 0
            return id
        }

        String toString(){
            "maxId: $maxId nextId: ${nextId.get()}"
        }
    }

    void setGenerator(IdGenerator generator) {
        if(this.generator) throw new IllegalArgumentException("IdGenerator is already assigned, no hot swapping")
        this.generator = generator
    }

    private IdGenerator getGenerator() {
        return generator
    }

}
