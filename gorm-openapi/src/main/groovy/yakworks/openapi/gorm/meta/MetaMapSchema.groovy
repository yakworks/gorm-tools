/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.openapi.gorm.meta

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j

import io.swagger.v3.oas.models.media.Schema
import yakworks.commons.lang.NameUtils
import yakworks.commons.map.MapFlattener
import yakworks.meta.MetaEntity
import yakworks.openapi.gorm.OapiSupport

/**
 * Includes tree for root entity and nested association properties
 * See MetaMapSchemaSpec unit tests and rally-domain for how it works
 */
@Slf4j
@EqualsAndHashCode(includes=["rootClassName", "props"], useCanEqual=false) //because its used as cache key
@CompileStatic
class MetaMapSchema implements Serializable {
    //fully qualified root class
    String rootClassName
    //value will be either another MetaMapSchema or the Schema for the prop
    Map props = [:] as Map<String, Object>
    //used for openApi to add the schema into it.
    Schema schema

    OapiSupport oapiSupport

    MetaMapSchema(){
        this.oapiSupport = OapiSupport.instance
    }

    static MetaMapSchema of(MetaEntity mentity){
        def metaMapSchema = new MetaMapSchema()
        metaMapSchema.build(mentity)
    }

    MetaMapSchema build(MetaEntity metaEntity) {
        Schema rootSchema = oapiSupport.getSchema(metaEntity.shortClassName)
        if(!rootSchema) return this
        this.rootClassName = metaEntity.className
        this.schema = rootSchema
        def mmiProps = metaEntity.metaProps
        for (String key in mmiProps.keySet()) {
            def nestedIncs = mmiProps[key]
            if(nestedIncs instanceof MetaEntity) {
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
     * gets the root class name with out prefix so we can lookup the openapi schema
     */
    String getShortRootClassName(){
        return NameUtils.getShortName(rootClassName)
    }

    /**
     * gets the short root class prop name name, for example Org will be org.
     * used to prepend to do i18n look ups
     */
    String getRootClassPropName(){
        return NameUtils.getPropertyName(rootClassName)
    }

    /**
     * flatten schema map
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
