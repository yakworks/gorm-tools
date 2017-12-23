package gorm.tools.idgen

import groovy.transform.CompileStatic
import org.apache.commons.lang.Validate
import org.apache.log4j.Category

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.LongUnaryOperator
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
    Long defaultBatchSize = 5

    BatchIdGenerator() {
    }

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

        //if the tuple is cached already then update it as once its cached it only uses its batchSize property
        if (idTupleMap.containsKey(keyName))
            idTupleMap[keyName].get().setBatchSize(batchSize)
    }

    private final UnaryOperator<IdTuple> updateAndGetOperator = { IdTuple curCtr ->
        //check again if its atMax. another thread might have squeezed in and incremented it already
        if(curCtr.atomicId.get() >= curCtr.maxId){
            //getNextId in the generator we delegate to here must be syncronized
            long minId = getGenerator().getNextId(curCtr.keyName, curCtr.batchSize.get())
            //println "got next batch of ids with $curCtr.keyName ${curCtr.batchSize.get()} $minId"
            return new IdTuple(curCtr.keyName, minId, curCtr.batchSize.get())
        } else {
            long id = curCtr.atomicId.getAndIncrement()
            return curCtr
        }

    } as UnaryOperator<IdTuple>

    long getNextId(String keyName) {
        AtomicReference<IdTuple> idAtomic = findOrCreate(keyName)
        while (true) {
            IdTuple oldTuple = idAtomic.get()
            long oldId = idAtomic.get().atomicId.get()
            long newId
            if (oldId >= oldTuple.maxId)) {
                getGenerator().getNextId(oldTuple.keyName, oldTuple.batchSize.get())
                
                //println "got next batch of ids with $curCtr.keyName ${curCtr.batchSize.get()} $minId"
                new IdTuple(oldTuple.keyName, minId, oldTuple.batchSize.get())
            }
                throw new IllegalArgumentException(
                    "Can't set lower to " + i + " > upper");
            IntPair newv = new IntPair(i, oldv.upper);
            if (values.compareAndSet(oldv, newv))
                return;
        }
        return idAtomic.updateAndGet(updateAndGetOperator).atomicId.get()
        //return updateAndGet(idAtomic)
        //check to see if its at its max. if so then go to the (jdbcBatchIdGen) and get a new bbatch range.
//        if (idAtomic.get().atMax()) {
//            return updateAndGet(idAtomic)
//        }
//        return idAtomic.get().getAndIncrement()
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
//    long updateAndGet(AtomicReference<IdTuple> idAtomic) {
//
//        Closure update = { IdTuple curCtr ->
//            //check again if its atMax. another thread might have squeezed in and incremented it already
//            if(curCtr.atMax()){
//                //getNextId in the generator we delegate to here must be syncronized
//                long minId = getGenerator().getNextId(curCtr.keyName, curCtr.batchSize.get())
//                //println "got next batch of ids with $keyName $batchSize $curId"
//                return new IdTuple(curCtr.keyName, minId, curCtr.batchSize.get())
//            }
//            return curCtr
//        }
//
//        return idAtomic.updateAndGet(update as UnaryOperator<IdTuple>).getAndIncrement()
//    }

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

                idTupleMap.put(keyName, new AtomicReference<IdTuple>(new IdTuple(keyName, current, batchSize)))
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
    void reserveIds(String keyName, long batchSize) {
        synchronized (keyName.intern()) {
            log.debug("Creating a BatchIDGenerator.IdTuple for " + keyName)

            idTupleMap.put(keyName,
                new AtomicReference<IdTuple>(
                    new IdTuple(
                        keyName,
                        getGenerator().getNextId(keyName, batchSize),
                        batchSize)
                )
            )
        }
    }

    // MARKER holder class for cached id info
    class IdTuple {
        final String keyName
        final long maxId //if nextID reaches this point it time to hit the db(generator) for a new set of values
        final AtomicLong atomicId
        final AtomicLong batchSize //atomic as it can get updated later

        IdTuple(String keyName, long currentId, long batchSize) {
            this.keyName = keyName
            this.atomicId = new AtomicLong(currentId)
            this.batchSize = new AtomicLong(batchSize)
            this.maxId = currentId + batchSize - 1
        }

        void setBatchSize(long size){
            batchSize.set(size)
        }

        boolean atMax(){
            atomicId.get() >= maxId
        }

        int getRemainingIds(){
            (maxId + 1) - atomicId.get()
        }

        private final LongUnaryOperator updateAndGetOperator = { Long curId ->
            //check again if its atMax. another thread might have squeezed in and incremented it already
            if(curId >= maxId){
                //getNextId in the generator we delegate to here must be syncronized
                long minId = getGenerator().getNextId(keyName, batchSize.get())
                //println "got next batch of ids with $curCtr.keyName ${curCtr.batchSize.get()} $minId"
                return minId
            }
            return curId
        } as LongUnaryOperator

        public final long getAndUpdate() {
            long prev, next;
            do {
                prev = get();
                next = updateFunction.applyAsLong(prev);
            } while (!compareAndSet(prev, next));
            return prev;
        }
        long getAndIncrement() {
            long id = atomicId.getAndIncrement()
            if(id > maxId) throw new IllegalStateException("ID can't be greater than maxId")
            return id
        }
    }


    void setGenerator(IdGenerator generator) {
        Validate.isTrue(this.generator == null, "IdGenerator is already created, no hot swapping")
        this.generator = generator
    }

    private IdGenerator getGenerator() {
        return generator
    }

}
