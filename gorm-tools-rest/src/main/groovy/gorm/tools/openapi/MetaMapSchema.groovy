/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.beans.map.MetaMapIncludes
import io.swagger.v3.oas.models.media.Schema
import yakworks.commons.lang.NameUtils

/**
 * Includes tree for root entity and nested association properties
 * See unit tests in rally-domain for how it works
 */
@Slf4j
@CompileStatic
class MetaMapSchema {
    String className
    //value will be either another MetaMapSchema or the Schema for the prop
    Map props = [:] as Map<String, Object>
    //used for openApi to add the schema into it.
    Schema schema

    OapiSupport oapiSupport

    MetaMapSchema(){
        this.oapiSupport = OapiSupport.instance()
    }

    static MetaMapSchema of(MetaMapIncludes mmi){
        def metaMapSchema = new MetaMapSchema()
        metaMapSchema.build(mmi)
    }

    MetaMapSchema build(MetaMapIncludes metaMapIncludes) {
        Schema rootSchema = oapiSupport.getSchema(metaMapIncludes.shortClassName)
        this.className = metaMapIncludes.className
        this.schema = rootSchema
        def mmiProps = metaMapIncludes.props
        for (String key in mmiProps.keySet()) {
            MetaMapIncludes nestedIncs = mmiProps[key]
            if(nestedIncs) {
                this.props[key] = MetaMapSchema.of(nestedIncs).props
            } else {
                this.props[key] = rootSchema.properties[key]
            }
        }
        return this
    }

    /**
     * Filters the props to only the ones that are association and have a nested includes
     */
    Map<String, MetaMapSchema> getNestedIncludes(){
        return props.findAll { it.value != null} as Map<String, MetaMapSchema>
    }

    /**
     * gets the class name with out prefix sowe can lookup the openapi schema
     */
    String getShortClassName(){
        return NameUtils.getShortName(className)
    }

    /**
     * Filters the props to only the ones that are association and have a nested includes
     */
    // Map<String, Schema> flatten(){
    //     Map<String, Schema> keyValues = [:]
    //
    //     if (!props) {
    //         return keyValues
    //     }
    //     keyValues.putAll(transformGroovyJsonMap(props, ""))
    //
    //     return keyValues
    // }



}
