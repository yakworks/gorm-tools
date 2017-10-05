package gorm.tools.idgen

public interface IdGenerator {

	/**
	 * Convenience method and should usually call getNexId(name,increment) with increment set to 1
	 * 
	 * @param keyName the key value of the id. usually of form "table.id"
	 * @return
	 */
	public long getNextId(String keyName)

	/**
	 * 
	 * @param keyName the key value of the id. usually of form "table.id"
	 * @param increment the number to increment the id by. this will happen after the value is returned. 
	 * 	in other words if you call getNextId("TableName.id",100) and get back a value of 10 then you can safely use
	 * 	ids from 10 to 109. The next request by your thread or any others to this method will return 110.
	 * @return
	 */
	public long getNextId(String keyName, long increment)
}