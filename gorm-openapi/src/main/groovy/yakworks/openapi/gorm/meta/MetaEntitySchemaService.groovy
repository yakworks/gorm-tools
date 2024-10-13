/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.openapi.gorm.meta

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.annotation.Cacheable

import gorm.tools.metamap.MetaGormEntityBuilder
import gorm.tools.metamap.services.MetaEntityService
import io.swagger.v3.oas.models.media.Schema
import yakworks.meta.MetaEntity
import yakworks.meta.MetaProp
import yakworks.openapi.gorm.OapiSupport

/**
 * Overrides the MetaEntityService to add the schema props on MetaEntity
 */
@Slf4j
@CompileStatic
class MetaEntitySchemaService extends MetaEntityService {

    OapiSupport getOapiSupport(){
        return OapiSupport.instance
    }

    /**
     * Overrides MetaEntityService to add the Oapi schema and return the extended MetaMapSchemaIncludes
     */
    //@Cacheable('MetaEntity')
    @Override
    MetaEntity getMetaEntity(String entityClassName, List<String> includes, List<String> excludes) {
        //gets base
        def metaEntity = MetaGormEntityBuilder.build(entityClassName, includes, excludes)
        if(metaEntity && oapiSupport.hasSchema()) updateSchema(metaEntity)
        return metaEntity
    }

    /**
     * updates the metaEntity with the schema if it can be found
     */
    MetaEntity updateSchema(MetaEntity metaEntity) {
        Schema rootSchema = oapiSupport.getSchema(metaEntity.shortClassName)
        if(!rootSchema) return metaEntity
        //set the schema
        metaEntity.schema = rootSchema
        //update the props with the schema props
        def mmiProps = metaEntity.metaProps
        for (String key in mmiProps.keySet()) {
            def val = mmiProps[key]
            if(val instanceof MetaEntity) {
                updateSchema(val)
            } else {
                //it should be an instance of the MetaProp
                ((MetaProp)val).schema = rootSchema.properties[key]
            }
        }
        return metaEntity
    }

}
