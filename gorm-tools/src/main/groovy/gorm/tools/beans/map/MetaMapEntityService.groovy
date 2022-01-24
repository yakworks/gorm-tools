/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans.map

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.api.IncludesConfig
import gorm.tools.beans.AppCtx
import grails.plugin.cache.Cacheable
import yakworks.commons.json.JsonEngine

/**
 * EntityMapService contains a set of helpers, which will create the EntityMap and Lists
 * from Gorm Domains
 */
@Slf4j
@CompileStatic
class MetaMapEntityService {

    @Autowired IncludesConfig includesConfig

    //static cheater to get the bean, use sparingly if at all
    static MetaMapEntityService get(){
        AppCtx.get('metaMapEntityService', this)
    }

    /**
     * Wrap entity/object in EntityMap
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    MetaMap createMetaMap(Object entity, List<String> includes = null, List<String> excludes = null) {
        MetaMapIncludes includesMap = buildIncludes(entity.class.name, includes, excludes)
        return new MetaMap(entity, includesMap)
    }

    /**
     * Calls createEntityMap and then passed to JsonEngine to generate json string
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the json string
     */
    String toJson(Object entity, List<String> includes = null, List<String> excludes = null){
        MetaMap emap = createMetaMap(entity, includes, excludes)
        return JsonEngine.toJson(emap)
    }

    String toJson(List entityList, List<String> includes = null, List<String> excludes = null){
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
    MetaMapList createMetaMapList(List entityList, List<String> includes, List<String> excludes = null) {
        if(entityList) {
            //use first item to get the class
            Class entityClass = entityList[0].class.name
            MetaMapIncludes includesMap = buildIncludes(entityClass.name, includes, excludes)
            return new MetaMapList(entityList, includesMap)
        }
        // return empty list
        return new MetaMapList([])
    }

    /**
     * wrapper around buildIncludesMap that uses a cache
     *
     * @param entityClassName the entity to wrap in a map
     * @param includes the includes list in dot notation
     * @return the created EntityMapIncludes
     */
    @Cacheable('entityMapIncludes')
    MetaMapIncludes getMetaMapIncludes(String entityClassName, List<String> includes = []) {
        return buildIncludes(entityClassName, includes)
    }

    /**
     * buildIncludesMap with class
     */
    MetaMapIncludes buildIncludes(Class entityClazz, List<String> includes = []) {
        buildIncludes(entityClazz.name, includes)
    }

    /**
     * builds a EntityMapIncludes object from a sql select like list. Used in EntityMap and EntityMapList
     *
     * @param className the class name of the PersistentEntity
     * @param includes the includes list in our custom dot notation
     * @return the EntityMapIncludes object that can be passed to EntityMap
     */
    MetaMapIncludes buildIncludes(String entityClassName, List<String> includes = []) {
        return EntityIncludesBuilder.build(entityClassName, includes)
    }

    MetaMapIncludes buildIncludes(String entityClassName, List<String> includes, List<String> excludes) {
        return EntityIncludesBuilder.build(entityClassName, includes, excludes)
    }

}
