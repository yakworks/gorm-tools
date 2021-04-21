/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.idgen

/**
 * not used but interface for a generator that uses strings
 */
interface StringIdGenerator {

    String getNewId(String tranTypeName)

}
