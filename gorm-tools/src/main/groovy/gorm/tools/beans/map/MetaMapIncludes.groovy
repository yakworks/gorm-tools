/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans.map

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Includes tree for root entity and nested association properties
 */
@Slf4j
@CompileStatic
class MetaMapIncludes {
    String className
    Set<String> fields
    Set<String> excludeFields
    //nestedIncludes has the associations and its included fields
    Map<String, MetaMapIncludes> nestedIncludes

    MetaMapIncludes(Set<String> fields){
        this.fields = fields
    }

    MetaMapIncludes(String className, Set<String> fields, Set<String> excludeFields){
        this.className = className
        this.fields = fields - excludeFields
        this.excludeFields = excludeFields
    }

    static MetaMapIncludes of(List<String> fields){
        new MetaMapIncludes(fields as Set)
    }
}
