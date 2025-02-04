/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

import yakworks.meta.MetaEntity
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList

/**
 * MetaMap.Converter for List to just show id for the gormEntity if nothing is specified.
 */
@CompileStatic
class GormEntityListConverter implements MetaMap.Converter, Serializable {

    @Override
    boolean handles(Object value) {
        if(Collection.isAssignableFrom(value.class)) {
            def valList = value as Collection
            if (valList.size() != 0 && valList[0] instanceof GormEntity) {
                return true
            }
        }
        return false
    }

    @Override
    Object convert(Object value, String key) {
        def valList = value as List
        def val = new MetaMapList(valList, MetaEntity.of(['id']))
        return val
    }

}
