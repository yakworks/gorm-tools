/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.idgen

/**
 * base insterface to be implemented for id generation.
 */
interface IdGenerator {

    /**
     * Convenience method and should usually call getNexId(name,increment) with increment set to 1
     *
     * @param keyName the key value of the id. usually of form "table.id"
     * @return the new id
     */
    long getNextId(String keyName)

    /**
     * @param keyName the key value of the id. usually of form "table.id"
     * @param increment the number to increment the id by. this will happen after the value is returned.
     *  in other words if you call getAndIncrement("TableName.id",100) and get back a value of 10 then you can safely use
     *  ids from 10 to 109. The next request by your thread or any others to this method will return 110.
     * @return the new id
     */
    long getNextId(String keyName, long increment)
}
