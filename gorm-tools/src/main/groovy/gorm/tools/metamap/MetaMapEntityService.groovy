/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.annotation.Cacheable

import gorm.tools.beans.AppCtx
import yakworks.json.groovy.JsonEngine

/**
 * EntityMapService contains a set of helpers, which will create the EntityMap and Lists
 * from Gorm Domains
 */
@Slf4j
@CompileStatic
class MetaMapEntityService {

    //static cheater to get the bean, use sparingly if at all
    static MetaMapEntityService bean(){
        AppCtx.get('metaMapEntityService', this)
    }

    /**
     * Wrap entity/object in EntityMap
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    MetaMap createMetaMap(Object entity, List<String> includes = [], List<String> excludes = []) {
        MetaMapIncludes includesMap = bean().getCachedMetaMapIncludes(entity.class.name, includes, excludes)
        return new MetaMap(entity, includesMap)
    }

    /**
     * Calls createEntityMap and then passed to JsonEngine to generate json string
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the json string
     */
    String toJson(Object entity, List<String> includes = [], List<String> excludes = []){
        MetaMap emap = createMetaMap(entity, includes, excludes)
        return JsonEngine.toJson(emap)
    }

    String toJson(List entityList, List<String> includes = [], List<String> excludes = []){
        MetaMapList elist = createMetaMapList(entityList, includes, excludes)
        return JsonEngine.toJson(elist)
    }


    /**
     * Wrap list in EntityMapList
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    MetaMapList createMetaMapList(List entityList, List<String> includes, List<String> excludes = []) {
        if(entityList) {
            //use first item to get the class
            Class entityClass = entityList[0].class.name
            MetaMapIncludes includesMap = bean().getCachedMetaMapIncludes(entityClass.name, includes, excludes)
            return new MetaMapList(entityList, includesMap)
        }
        // return empty list
        return new MetaMapList([])
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
    MetaMapIncludes getCachedMetaMapIncludes(String entityClassName, List<String> includes, List<String> excludes) {
        return MetaMapIncludesBuilder.build(entityClassName, includes, excludes)
    }

}
