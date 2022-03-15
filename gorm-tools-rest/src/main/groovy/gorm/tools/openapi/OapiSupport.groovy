/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi

import java.nio.file.Paths

import groovy.transform.CompileStatic

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import yakworks.commons.util.BuildSupport

/**
 * exposes a parsed openApi yaml
 */
@SuppressWarnings(['PropertyName'])
@CompileStatic
class OapiSupport {
    //assumed this is on the classpath
    public static String OAPI_SRC = 'oapi.yaml'

    OpenAPI openAPI

    OapiSupport(){
        openAPI = new OpenAPIV3Parser().read(OAPI_SRC)
    }

    private static OapiSupport _oapiSupport

    static OapiSupport instance(){
        if(!_oapiSupport) _oapiSupport = new OapiSupport()
        return _oapiSupport
    }

    Schema getSchema(String name){
        openAPI.getComponents().schemas[name]
    }

    Schema getSchemaProps(String name){
        openAPI.getComponents().schemas[name]
    }

    Schema getSchemaForPath(Schema entitySchema, String propKey){
        if(!propKey.contains('.')) return entitySchema.properties[propKey]

        Schema result = propKey.tokenize('.').inject(entitySchema) { Schema curSchema, String prop ->
            Schema value = curSchema.properties[prop]
            if (value.$ref){
                value = resolveSchema(value.$ref)
            }
            return value
        }
        result
    }

    Schema resolveSchema(String ref) {
        String simpleName = (String) RefUtils.extractSimpleName(ref).getLeft()
        Schema model = openAPI.components.schemas.get(simpleName)
        return model
    }

    /**
     * Takes a list or Maps in form
     * [
     *  { key:'contact.org' }
     * ]
     * and add in properties from schema api
     *
     */
    List<Map> build(String root, List<Map> config){
        Schema rootSchema = openAPI.getComponents().schemas[root]
        List mergedConfig = [] as List<Map>

        config.each{ Map entry ->
            String key = entry['key']
            def props = getSchemaProps(key)
        }
    }

    static OapiSupport of(String root){
        def path = Paths.get(BuildSupport.gradleRootProjectDir?:'', OAPI_SRC)
        ParseOptions options = new ParseOptions()
        options.resolve = true
        options.resolveFully = true
        def sf = new OapiSupport()
        sf.openAPI = new OpenAPIV3Parser().read(path.toString(), null, options)

        return sf
    }

}
