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

    //this is a thread safe hashmap
    private ConcurrentMap<String, AtomicReference<IdTuple>> entries = new ConcurrentHashMap<String, AtomicReference<IdTuple>>()
    private IdGenerator generator
    private long batchSize = 255

    BatchIdGenerator() {
    }

    BatchIdGenerator(IdGenerator generator) {
        setGenerator(generator)
    }

    void setAllocationSize(long batchSize) {
        this.batchSize = batchSize
    }

    long getNextId(String name) {
        return getNextId(name, 1)
    }


    long getNextId(String keyName, long increment) {

        AtomicReference<IdTuple> idAtomic = findOrCreate(keyName)

        //check to see if its at its max. if so then go to the (jdbcBatchIdGen) and get a new bbatch range.
        if (idAtomic.get().atMax()) {

            Closure update = { IdTuple curCtr ->
                //check again if its atMax. another thread might have jumped in and done it by the time this is run
                if(curCtr.atMax()){
                    //getNextId in the generator we delegate to here must be syncronized
                    long curId = getGenerator().getNextId(keyName, batchSize)
                    //println "got next batch of ids with $keyName $batchSize $curId"
                    return new IdTuple(keyName, curId, batchSize)
                }
                return curCtr
            }

            return idAtomic.updateAndGet(update as UnaryOperator<IdTuple>).getAndIncrement()
        }
        return idAtomic.get().getAndIncrement()
    }

    private AtomicReference<IdTuple> findOrCreate(String keyName) {
        Validate.notNull(keyName, "The row key name can't be null")

        if (!entries.containsKey(keyName)) {
            //synchronize on the keyname. itern forces it to use same string. see http://java-performance.info/string-intern-java-6-7-8-multithreaded-access/
            synchronized (keyName.intern()) {
                log.debug("Creating a BatchIDGenerator.IdTuple for " + keyName)
                //go to the (jdbcBatchIdGen) and get a new batch range.
                long current = getGenerator().getNextId(keyName, batchSize)
                AtomicReference<IdTuple> aidc = new AtomicReference<IdTuple>(new IdTuple(keyName, current, batchSize))
                entries.putIfAbsent(keyName, aidc)
            }

        }
        return entries.get(keyName)
    }

    void setGenerator(IdGenerator generator) {
        Validate.isTrue(this.generator == null, "IdGenerator is already created")
        this.generator = generator
    }

    private IdGenerator getGenerator() {
        return generator
    }

    // MARKER holder class for cached id info
    class IdTuple {
        private final String keyName
        private final long maxId //if nextID reaches this point it time to hit the db(generator) for a new set of values
        final AtomicLong atomicId

        IdTuple(String keyName, long currentId, long batchSize) {
            this.keyName = keyName
            this.atomicId = new AtomicLong(currentId)
            this.maxId = currentId + batchSize - 1
        }

        boolean atMax(){
            atomicId.get() >= maxId
        }

        long getAndIncrement() {
            return atomicId.getAndIncrement()
        }
    }

}
