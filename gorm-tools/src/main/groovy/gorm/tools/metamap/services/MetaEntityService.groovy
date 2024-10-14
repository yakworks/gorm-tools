/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap.services

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.annotation.Cacheable

import gorm.tools.metamap.MetaGormEntityBuilder
import yakworks.meta.MetaEntity

/**
 * MetaEntityService Spring service for building MetaEntity
 * Cacheable wrapper around MetaGormEntityBuilder
 */
@Slf4j
@CompileStatic
class MetaEntityService {

    /**
     * convenience util for getMetaEntity
     * @see #getMetaEntity(String, List, List)
     */
    // MetaEntity getMetaEntity(Class clazz, List<String> includes, List<String> excludes) {
    //     return getMetaEntity(clazz.name, includes, excludes)
    // }

    /**
     * wrapper around MetaGormEntityBuilder.build that checks cache first
     *
     * @param entityClassName the entity to wrap in a map
     * @param includes the includes list in dot notation
     * @param excludes the excludes list in dot notation
     * @return the created EntityMapIncludes
     */
    @Cacheable('MetaEntity')
    MetaEntity getMetaEntity(String entityClassName, List<String> includes, List<String> excludes) {
        return MetaGormEntityBuilder.build(entityClassName, includes, excludes)
    }

}
