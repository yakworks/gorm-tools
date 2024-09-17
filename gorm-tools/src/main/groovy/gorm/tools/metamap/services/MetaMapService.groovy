/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap.services

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import yakworks.api.problem.data.DataProblem
import yakworks.meta.MetaEntity
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList

/**
 * MetaMapService contains a set of helpers, which will create the MetaMap and Lists from Gorm Domains.
 * A MetaMap is a Map that wraps an object tree and reads properties from a entity based on list of includes/excludes
 * The flow is to create a MetaEntity first (which is the meta data about the entity, what to include, titles, etc..)
 * and then uses that to instantiate the MetaMap or MetaList.
 */
@Slf4j
@CompileStatic
class MetaMapService {

    @Autowired MetaEntityService metaEntityService

    /**
     * Wrap entity/object in MetaMap
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    MetaMap createMetaMap(Object entity, List<String> includes = [], List<String> excludes = []) {
        MetaEntity metaEntity = metaEntityService.getMetaEntity(entity.class.name, includes, excludes)
        //XXX Temp hack for now, metaEntity should never be null here unless its a Map, then its ok.
        if(!Map.isAssignableFrom(entity.class) && !metaEntity)
            throw DataProblem.ex("Problem creating the entity map, the includes parameter may have invalid properties")

        return new MetaMap(entity, metaEntity)
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
            MetaEntity metaEntity = metaEntityService.getMetaEntity(entityClass.name, includes, excludes)
            //XXX Temp hack for now, metaEntity should never be null here unless its a Map, then its ok.
            if(!Map.isAssignableFrom(entityClass) && !metaEntity)
                throw DataProblem.ex("Problem creating the entity map, the includes parameter may have invalid properties")

            return new MetaMapList(entityList, metaEntity)
        }
        // return empty list
        return new MetaMapList([])
    }

}
