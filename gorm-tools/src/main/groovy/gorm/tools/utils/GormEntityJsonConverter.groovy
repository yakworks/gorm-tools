/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

/**
 * Fall through if GormEntity makes it to Json conversion it needs the misc conditions filtered out.
 */
@CompileStatic
class GormEntityJsonConverter implements JsonGenerator.Converter {
    //default is zero. higher is latter in list and lower priority. first converter found in groovy json wins.
    //we dont want this one getting picked up first, only as fall back.
    int order = 100

    @Override
    boolean handles(Class<?> type) {
        GormEntity.isAssignableFrom(type)
    }

    @Override
    Object convert(Object value, String key) {
        return GormMetaUtils.getProperties((GormEntity)value)
    }

}
