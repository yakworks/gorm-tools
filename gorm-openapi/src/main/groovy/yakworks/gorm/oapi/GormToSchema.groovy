/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.oapi

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.PersistentEntity

import gorm.tools.utils.GormMetaUtils

import static ApiSchemaEntity.CruType

/**
 * Generates the domain part
 * should be merged with either Swaggydocs or Springfox as outlined
 * https://github.com/OAI/OpenAPI-Specification is the new openAPI that
 * Swagger moved to.
 * We are chasing this part https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schemaObject
 * Created by JBurnett on 6/19/17.
 */
//@CompileStatic
@SuppressWarnings(['UnnecessaryGetter', 'AbcMetric', 'Println'])
@CompileStatic
class GormToSchema {

    //static final Map<String, Map> SCHEMA_CACHE = new ConcurrentHashMap<String, Map>()

    //good overview here
    //https://spacetelescope.github.io/understanding-json-schema/index.html
    //https://docs.spring.io/spring-data/rest/docs/current/reference/html/#metadata.json-schema
    //https://github.com/OAI/OpenAPI-Specification
    void generateModels() {
        def mapctx = GormMetaUtils.getMappingContext()
        //first generate the main one which will be readOnly without associations
        for( PersistentEntity entity : mapctx.persistentEntities){
            generate(entity, CruType.Read)
        }
    }
    //
    Map generate(Class clazz, CruType kind = CruType.Read) {
        PersistentEntity entity = GormMetaUtils.getPersistentEntity(clazz)
        return generate(entity, kind)
    }

    Map generate(PersistentEntity entity, CruType kind = CruType.Read) {
        ApiSchemaEntity apiSchemaEntity = new ApiSchemaEntity(entity)
        return apiSchemaEntity.generate(kind)
    }
}
