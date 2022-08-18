/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap.services

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import yakworks.meta.MetaMap
import yakworks.meta.MetaMapIncludes
import yakworks.meta.MetaMapList

/**
 * MetaMapService contains a set of helpers, which will create the EntityMap and Lists
 * from Gorm Domains
 */
@Slf4j
@CompileStatic
class MetaMapService {

    @Autowired MetaMapIncludesService metaMapIncludesService

    /**
     * Wrap entity/object in MetaMap
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    MetaMap createMetaMap(Object entity, List<String> includes = [], List<String> excludes = []) {
        MetaMapIncludes includesMap = metaMapIncludesService.getMetaMapIncludes(entity.class.name, includes, excludes)
        return new MetaMap(entity, includesMap)
    }

    /**
     * Wrap list in MetaMapList
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    MetaMapList createMetaMapList(List entityList, List<String> includes, List<String> excludes = []) {
        if(entityList) {
            //use first item to get the class
            Class entityClass = entityList[0].class.name
            MetaMapIncludes includesMap = metaMapIncludesService.getMetaMapIncludes(entityClass.name, includes, excludes)
            return new MetaMapList(entityList, includesMap)
        }
        // return empty list
        return new MetaMapList([])
    }

}
