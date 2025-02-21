/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

import gorm.tools.utils.GormMetaUtils
import yakworks.meta.MetaMap

/**
 * MetaMap.Converter default to just show id for the gormEntity if nothing is specified.
 *
 */
@CompileStatic
class GormEntityConverter implements MetaMap.Converter {

    @Override
    boolean handles(Object value) {
        GormEntity.isAssignableFrom(value.class)
    }

    @Override
    Object convert(Object value, String key) {
        def obj = (GormEntity)value
        return GormMetaUtils.getIdMap(obj)
    }

}
