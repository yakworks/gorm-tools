package gorm.tools.idgen

import groovy.transform.CompileStatic
import org.apache.commons.lang.Validate
import org.apache.log4j.Category

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicLongArray
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
    Long defaultBatchSize = 255

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
    }
    long getBatchSize(String keyName) {
        if (batchSizeByKey.containsKey(keyName))
            return batchSizeByKey[keyName]
        else
            return defaultBatchSize
    }

//    private final UnaryOperator<IdTuple> updateAndGetOperator = { IdTuple curCtr ->
//        //check again if its atMax. another thread might have squeezed in and incremented it already
//        if(curCtr.atomicId.get() >= curCtr.maxId){
//            //getNextId in the generator we delegate to here must be syncronized
//            long minId = getGenerator().getNextId(curCtr.keyName, curCtr.batchSize.get())
//            //println "got next batch of ids with $curCtr.keyName ${curCtr.batchSize.get()} $minId"
//            return new IdTuple(curCtr.keyName, minId, curCtr.batchSize.get())
//        } else {
//            long id = curCtr.atomicId.getAndIncrement()
//            return curCtr
//        }
//
//    } as UnaryOperator<IdTuple>

    long getNextId(String keyName) {
        AtomicReference<IdTuple> tuple = findOrCreate(keyName)
        getAndUpdate(keyName, tuple)

//        if(tuple.get(2) > 0){
//            synchronized (keyName.intern()) {
//                //getNextId in the generator we delegate to here must be syncronized
//                long newid = getGenerator().getNextId(keyName, batchSize)
//                tuple.set(0,newid) //nextId
//                tuple.set(1, newid + batchSize - 1) //maxId
//                tuple.set(2,0)
//                //println "got next batch of ids with ${tuple.get(0)} : ${tuple.get(1)}"
//            }
//        }
//        id = tuple.getAndIncrement(0)
//
//        return id
//        if(cur[0] > cur[1]){
//            //getNextId in the generator we delegate to here must be syncronized
//            long id = getGenerator().getNextId(curCtr.keyName, batchSize)
//            //println "got next batch of ids with $curCtr.keyName ${curCtr.batchSize.get()} $minId"
//            return [ (id + 1), (id + batchSize - 1)] as long[]
//        }
//        long[] tup = idAtomic.updateAndGet(0, { long[] cur ->
//            //check again if its atMax. another thread might have squeezed in and incremented it already
//            if(cur[0] > cur[1]){
//                //getNextId in the generator we delegate to here must be syncronized
//                long id = getGenerator().getNextId(curCtr.keyName, batchSize)
//                //println "got next batch of ids with $curCtr.keyName ${curCtr.batchSize.get()} $minId"
//                return [ (id + 1), (id + batchSize - 1)] as long[]
//            } else {
//                return [ cur[0] + 1, cur[1]] as long[]
//            }
//        } as UnaryOperator<IdTuple>)
//
//        return tup.nextId.get()

//        while (true) {
//            IdTuple curTuple = idAtomic.get()
//            long id = curTuple.nextId
//            IdTuple newTup
//            if (id > curTuple.maxId) {
//                //println "===== id >= curTuple.maxId: id:$id , curTuple.maxId: ${curTuple.maxId}"
//                id = getGenerator().getNextId(keyName, batchSize)
//                newTup = new IdTuple(keyName, id + 1, id + batchSize - 1)
//                //println "**** after getGenerator id: $id , maxsize: ${newTup.maxId}"
//            } else{
//                newTup = new IdTuple(keyName, id + 1, curTuple.maxId)
//            }
//            if (idAtomic.compareAndSet(curTuple, newTup)){
//                //println "ADDED keyName: $keyName , id: $id , maxsize: ${newTup.maxId} "
//                return id
//            } else{
//                //println "!!!!!! FAILED id: $id "
//            }
//        }
        //return idAtomic.updateAndGet(updateAndGetOperator).atomicId.get()
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
    long getAndUpdate(String keyName, AtomicReference<IdTuple> idAtomic) {

        UnaryOperator updateOp = { IdTuple curCtr ->
            //check again if its atMax. another thread might have squeezed in and incremented it already
            if(curCtr.atMax){
                IdTuple newTuple
                synchronized (keyName.intern()) {
                    long batchSize = getBatchSize(keyName)
                    //getNextId in the generator we delegate to here must be syncronized
                    long nextId = getGenerator().getNextId(keyName, batchSize)
                    //println "got next batch of ids with $keyName $batchSize $curId"
                    newTuple = new IdTuple(nextId, nextId + batchSize - 1)
                }
                return newTuple
            }
            return new IdTuple(curCtr.nextId + 1 , curCtr.maxId)
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
        //final boolean needsRefresh = false
        //final AtomicLong batchSize //atomic as it can get updated later

        IdTuple(long currentId, long maxId) {
            this.nextId = currentId //new AtomicLong(currentId)
            this.maxId = maxId
            if(nextId == maxId) atMax = true
            if(nextId > maxId) throw new IllegalStateException("ID can't be greater than maxId")
        }

//        void setBatchSize(long size){
//            batchSize.set(size)
//        }
//
        boolean atMax(){
            //if(nextId > maxId) throw new IllegalStateException("ID can't be greater than maxId")
            nextId == maxId
        }
//
//        int getRemainingIds(){
//            (maxId + 1) - atomicId.get()
//        }

//        private final LongUnaryOperator updateAndGetOperator = { Long curId ->
//            //check again if its atMax. another thread might have squeezed in and incremented it already
//            if(curId >= maxId){
//                //getNextId in the generator we delegate to here must be syncronized
//                long minId = getGenerator().getNextId(keyName, batchSize.get())
//                //println "got next batch of ids with $curCtr.keyName ${curCtr.batchSize.get()} $minId"
//                return minId
//            }
//            return curId
//        } as LongUnaryOperator
//
//        public final long getAndUpdate() {
//            long prev, next;
//            do {
//                prev = get();
//                next = updateFunction.applyAsLong(prev);
//            } while (!compareAndSet(prev, next));
//            return prev;
//        }
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
