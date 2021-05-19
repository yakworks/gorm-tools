/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Includes tree for root entity and nested association properties
 */
@Slf4j
@CompileStatic
class EntityMapIncludes {
    String className
    Set<String> fields
    Set<String> blacklist
    //nestedIncludes has the associations and its included fields
    Map<String, EntityMapIncludes> nestedIncludes

    EntityMapIncludes(String className, Set<String> fields, Set<String> blacklist){
        this.className = className
        this.fields = fields - blacklist
        this.blacklist = blacklist
    }
}
