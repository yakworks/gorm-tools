/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

/**
 * Fall through if GormEntity makes it to Json conversion. With the special fields added in with AST it will get a
 * stackoverflow if the groovy json engine tries to convert it.
 * Uses GormMetaUtils.getProperties to only get the relavent props.
 *
 * This is registered as a java service in META-INF/services/groovy.json.JsonGenerator$Converter.
 * Its picked up in groovy-commons's JsonEngine.
 * @see yakworks.json.groovy.JsonEngine#getConverters
 * @see GormMetaUtils#getProperties
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
