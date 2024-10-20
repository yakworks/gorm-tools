/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango


import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired

import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey

@CompileStatic
class QuickSearchSupport {

    @Autowired IncludesConfig includesConfig

    /**
     * Generates the QuickSearch Mango map for the entity class
     *
     * <pre>{@code
     * // Assuming the entityClass has this static setup, where id is Long and num is String
     * static Map includes = [
     *   qSearch: ['id', 'num']
     * ]
     *
     * // Then will run like this
     * buildSearchMap(KitchenSink, 'foo') =
     * [
     *   $or:[
     *     [id: [$eq:'foo']],
     *     [num: [$ilike:'foo%']]
     *   ]
     * ]
     * }</pre>
     *
     * @param entityClazz The gorm entity to look up
     * @param qText the text to search
     * @return the built mango map to add
     */
    Map<String, List<Map>> buildSearchMap(Class entityClazz, String qText){
        Map searchFields = getQSearchFields(entityClazz)
        return searchFields ? buildSearchMap(searchFields, qText) : [:]
    }

    /**
     * Generates the QuickSearch Mango map
     *
     * <pre>{@code
     * buildSearchMap([id: Long, num: String], 'foo') =
     * [
     *   $or:[
     *     [id: [$eq:'foo']],
     *     [num: [$ilike:'foo%']]
     *   ]
     * ]
     * }</pre>
     *
     * @param searchFields Map with key=field and the value is the field class type
     * @param qText the text to search
     * @return the biult mango map to add
     */
    Map<String, List<Map>> buildSearchMap(Map<String,Class> searchFields, String qText){
        if(!searchFields) return [:]

        List orList = [] as List<Map>

        searchFields.each { k, v ->
            if(v == String){
                orList << [
                    (k): [
                        '$ilike': "${qText}%".toString()
                    ]
                ]
            } else {
                orList << [
                    (k): [
                        '$eq': qText
                    ]
                ]
            }
        }

        Map criteriaMap = ['$or': orList] as Map<String, List<Map>>
        return criteriaMap
    }

    /**
     * Gets a map with keys as fields and the value as the property/field type
     * @param entityClazz persitant entity
     * @return the map
     */
    protected Map<String,Class> getQSearchFields(Class entityClazz) {
        List<String> qsFields =  includesConfig.getByKey(entityClazz, IncludesKey.qSearch)
        Map qsFieldMap = [:] as Map<String,Class>
        for(String field: qsFields){
            qsFieldMap[field] = getPropertyType(entityClazz, field)
        }
        return qsFieldMap
    }

    protected Class getPropertyType(Class entityClass, String prop){
        GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(entityClass)
        PersistentEntity entity = gormStaticApi.gormPersistentEntity
        entity.getPropertyByName(prop).type
    }

}
