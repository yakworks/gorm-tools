package gorm.tools.idgen

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import org.apache.log4j.Category

/**
 * An Thread safe implementation that caches a range of values in memory by the key name (ie: "tablename.id") 
 * Default cache allocation size is 50 but can be set to other values. requires another IdGenerator implementation to be 
 * set in the constructor. Usually a Jdbc type implmentation that will get the values for this class for both initialization
 * and when this runs out of allocated ids.
 * 
 * @author josh
 *
 */
//FIXME Sud - we need tests here?
public class BatchIdGenerator implements IdGenerator {
	private static Category log = Category.getInstance(BatchIdGenerator.class)

	//this is a thread safe hasmap
	private ConcurrentHashMap<String, IdRow> entries = new ConcurrentHashMap<String, IdRow>()
	private IdGenerator generator
	private long allocationSize = 100

	public BatchIdGenerator() {
	}

	public BatchIdGenerator(IdGenerator generator) {
		setGenerator(generator)
	}

	public void setAllocationSize(long batchSize) {
		this.allocationSize = batchSize
	}

	public long getNextId(String name){
		return getNextId(name, 1)
	}

	public synchronized long getNextId(String keyName, long increment){
		long r
		if(keyName == null) {
			// If the name is null at this point, it's either a pick list or we want it to fail.
			// If we throw an exception here, the error is virtually untraceable.
			// if it's a pick list (or other non-resolved ds) putting a zero here hurts nothing.
			r = 0
		} else {
			//if increment is bigger than batchsize then use the increment instead
			long newAllocationSize =increment>allocationSize?increment:allocationSize
			IdRow idrow

			idrow = findOrCreate(keyName,increment)

			long current = idrow.getNextId(increment)
			if(current >= idrow.max || idrow.nextId.get() > idrow.max  ) {
				current = getGenerator().getNextId(keyName, newAllocationSize)
				idrow.max = current + newAllocationSize
				idrow.nextId.set(current+increment)
			}
			r = current
		}
		if(log.isDebugEnabled()) {
			log.debug("Returning ID " + r + " for key '" + keyName + "'")
		}
		return r
	}

	@SuppressWarnings(['SynchronizedOnThis'])
	private IdRow findOrCreate(String name, long increment){
		if(entries == null) throw new NullPointerException("The entries hashmap is undefined!")
		if(name == null) throw new NullPointerException("The row name is null!")
		//if its there then return it and don't block
		if (entries.containsKey(name)) 
			return entries.get(name)

		synchronized(this){
			//make sure again that its not there in case another thread finished adding it since the containsKey check right above
			if (entries.containsKey(name)) {
				return entries.get(name)
			}else{
				long current = getGenerator().getNextId(name, increment>allocationSize?increment:allocationSize)
				IdRow idrow = new IdRow(name)
				idrow.max = current+allocationSize
				idrow.nextId.set(current)
				entries.put(name, idrow)
				return idrow
			}

		}
	}

	public void setGenerator(IdGenerator generator) {
		if(this.generator == null) {
			this.generator = generator
		} else {
			throw new IllegalArgumentException("IdGenerator is already created!")
		}
	}

	private IdGenerator getGenerator() {
		if(generator == null) {
			throw new NullPointerException("Generator not found!")
		}
		return generator
	}

	// MARKER holder class for cached id info
	class IdRow{
		public IdRow(String keyname) {
			this.keyName = keyname
			log.debug("Creating a BatchIDGenerator for " + keyName)
		}

		final String keyName
		long max		//if nextID reaches this point it time to hit the db(generator) for a new set of values
		AtomicLong nextId = new AtomicLong()

		public long getNextId(long increment) {
			return nextId.getAndAdd(increment)
		}
	}

}