/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap.services

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.annotation.Cacheable

import gorm.tools.metamap.MetaMapIncludesBuilder
import yakworks.meta.MetaMapIncludes

/**
 * MetaMapIncludesService Spring service for building MetaMapIncludes
 * Cacheable wrapper around MetaMapIncludesBuilder
 */
@Slf4j
@CompileStatic
class MetaMapIncludesService {

    /**
     * convenience util for getMetaMapIncludes
     * @see #getMetaMapIncludes(String, List, List)
     */
    MetaMapIncludes getMetaMapIncludes(Class clazz, List<String> includes, List<String> excludes) {
        return getMetaMapIncludes(clazz.name, includes, excludes)
    }

    /**
     * wrapper around MetaMapIncludesBuilder.build that checks cache first
     *
     * @param entityClassName the entity to wrap in a map
     * @param includes the includes list in dot notation
     * @param excludes the excludes list in dot notation
     * @return the created EntityMapIncludes
     */
    @Cacheable('MetaMapIncludes')
    MetaMapIncludes getMetaMapIncludes(String entityClassName, List<String> includes, List<String> excludes) {
        return MetaMapIncludesBuilder.build(entityClassName, includes, excludes)
    }

}
