/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.oapi


import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import io.swagger.v3.oas.models.media.Schema

/**
 * A "view" over top of the open api schema.
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@CompileStatic
class SchemaView {
    /**
     * The root schema for the view.
     */
    Schema rootSchema

    /**
     * List of properties, may be direct properties or path association references
     */
    List<String> includes

    /**
     * After build will key by the includes field with the relevant Schema mapped.
     */
    Map<String, Schema> props

    OapiSupport oapiSupport

    SchemaView(){
        oapiSupport = OapiSupport.instance()
    }

    List getRequired(){
        rootSchema.required
    }

    static SchemaView of(String rootName){
        def sv = new SchemaView()
        Schema root = sv.oapiSupport.getSchema(rootName)
        return sv.rootSchema(root)
    }

    static SchemaView of(Schema rootSchema){
        def sv = new SchemaView()
        return sv.rootSchema(rootSchema)
    }

    SchemaView build(){
        assert includes
        props = [:] as Map<String, Schema>
        for(String propKey: includes){
            // this is not the most efficient.
            if(propKey.contains('.')) {
                props[propKey] = oapiSupport.getSchemaForPath(rootSchema, propKey)
            } else {
                props[propKey] = rootSchema.properties[propKey]
            }
        }
    }


}
