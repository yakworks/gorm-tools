/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.beans.map.MetaMapIncludes
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.media.Discriminator
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.XML
import yakworks.commons.lang.NameUtils
import yakworks.commons.map.MapFlattener

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
    Map<String, Map> flatten(){
        Map flatMap = MapFlattener.flattenMap(props) as Map<String, Schema>
        Map map = [:] as Map<String, Map>
        //iterate over and convert Schema to Map
        flatMap.each{String k, Schema schema->
            Map schemaMap = [:] as Map<String, Object>
            List schemaAttrs = ['name', 'title', 'multipleOf',  'maximum', 'exclusiveMaximum', 'minimum',
                'exclusiveMinimum', 'maxLength', 'minLength', 'pattern', 'maxItems', 'minItems', 'uniqueItems',
                'maxProperties', 'minProperties', 'required', 'type', 'not', 'description', 'format', '$ref', 'nullable',
                'readOnly', 'writeOnly', 'example', 'enum']

            for(String attr: schemaAttrs){
                if(schema[attr] != null){
                    schemaMap[attr] = schema[attr]
                }
            }
            map[k] = schemaMap
        }
        return map
    }



}
