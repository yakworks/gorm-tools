/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi.metamap

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.annotation.Cacheable

import gorm.tools.metamap.MetaMapIncludes
import gorm.tools.metamap.MetaMapIncludesBuilder
import gorm.tools.metamap.MetaProp
import gorm.tools.metamap.services.MetaMapIncludesService
import gorm.tools.openapi.MetaMapSchema
import gorm.tools.openapi.OapiSupport
import io.swagger.v3.oas.models.media.Schema

/**
 * Overrides the MetaMapIncludesService to add the schema props on MetaMapIncludes
 */
@Slf4j
@CompileStatic
class MetaMapSchemaIncludesService extends MetaMapIncludesService {

    OapiSupport oapiSupport

    MetaMapSchemaIncludesService(){
        oapiSupport = OapiSupport.instance()
    }

    /**
     * Overrides MetaMapIncludesService to add the Oapi schema and return the extended MetaMapSchemaIncludes
     */
    @Cacheable('MetaMapIncludes')
    @Override
    MetaMapIncludes getMetaMapIncludes(String entityClassName, List<String> includes, List<String> excludes) {
        //gets base
        def metaMapIncludes = MetaMapIncludesBuilder.build(entityClassName, includes, excludes)
        updateSchema(metaMapIncludes)
        return metaMapIncludes
    }

    /**
     * updates the metaMapIncludes with the schema if it can be found
     */
    MetaMapIncludes updateSchema(MetaMapIncludes metaMapIncludes) {
        Schema rootSchema = oapiSupport.getSchema(metaMapIncludes.shortClassName)
        if(!rootSchema) return metaMapIncludes
        //set the schema
        metaMapIncludes.schema = rootSchema
        //update the props with the schema props
        def mmiProps = metaMapIncludes.propsMap
        for (String key in mmiProps.keySet()) {
            def val = mmiProps[key]
            if(val instanceof MetaMapIncludes) {
                updateSchema(val)
            } else {
                //it should be an instance of the MetaProp
                ((MetaProp)val).schema = rootSchema.properties[key]
            }
        }
        return metaMapIncludes
    }

}
