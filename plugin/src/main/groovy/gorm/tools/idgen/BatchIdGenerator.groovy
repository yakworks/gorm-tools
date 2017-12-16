package gorm.tools.idgen

import groovy.transform.CompileStatic
import org.apache.commons.lang.Validate
import org.apache.log4j.Category

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

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
    private Map<String, IdRow> entries = new ConcurrentHashMap<String, IdRow>()
    private IdGenerator generator
    private long allocationSize = 100

    BatchIdGenerator() {
    }

    BatchIdGenerator(IdGenerator generator) {
        setGenerator(generator)
    }

    void setAllocationSize(long batchSize) {
        this.allocationSize = batchSize
    }

    long getNextId(String name) {
        return getNextId(name, 1)
    }

    @SuppressWarnings(['SynchronizedMethod'])
    synchronized long getNextId(String keyName, long increment) {
        long r
        if (keyName == null) {
            // If the name is null at this point, it's either a pick list or we want it to fail.
            // If we throw an exception here, the error is virtually untraceable.
            // if it's a pick list (or other non-resolved ds) putting a zero here hurts nothing.
            r = 0
        } else {
            //if increment is bigger than batchsize then use the increment instead
            long newAllocationSize = increment > allocationSize ? increment : allocationSize
            IdRow idrow

            idrow = findOrCreate(keyName, increment)

            long current = idrow.getNextId(increment)
            if (current >= idrow.max || idrow.nextId.get() > idrow.max) {
                current = getGenerator().getNextId(keyName, newAllocationSize)
                idrow.max = current + newAllocationSize
                idrow.nextId.set(current + increment)
            }
            r = current
        }
        if (log.isDebugEnabled()) {
            log.debug("Returning ID " + r + " for key '" + keyName + "'")
        }
        return r
    }

    @SuppressWarnings(['SynchronizedOnThis'])
    private IdRow findOrCreate(String name, long increment) {
        Validate.notNull(entries, "The entries hashmap is undefined!")
        Validate.notNull(name, "The row key name can't be null")
        //if its there then return it and don't block
        if (entries.containsKey(name))
            return entries.get(name)

        synchronized (this) {
            //make sure again that its not there in case another thread finished adding it since the containsKey check right above
            if (entries.containsKey(name)) {
                return entries.get(name)
            }
            long current = getGenerator().getNextId(name, increment > allocationSize ? increment : allocationSize)
            IdRow idrow = new IdRow(name)
            idrow.max = current + allocationSize
            idrow.nextId.set(current)
            entries.put(name, idrow)
            return idrow
        }
    }

    void setGenerator(IdGenerator generator) {
        Validate.isTrue(this.generator == null, "IdGenerator is already created")
        this.generator = generator
    }

    private IdGenerator getGenerator() {
        return generator
    }

    // MARKER holder class for cached id info
    class IdRow {
        IdRow(String keyname) {
            this.keyName = keyname
            log.debug("Creating a BatchIDGenerator for " + keyName)
        }

        final String keyName
        long max        //if nextID reaches this point it time to hit the db(generator) for a new set of values
        AtomicLong nextId = new AtomicLong()

        long getNextId(long increment) {
            return nextId.getAndAdd(increment)
        }
    }

}
