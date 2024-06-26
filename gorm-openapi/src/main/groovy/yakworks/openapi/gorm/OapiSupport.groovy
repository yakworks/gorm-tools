/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.openapi.gorm

import java.nio.file.Paths

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.core.io.ClassPathResource

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
@Slf4j
@CompileStatic
class OapiSupport {
    //assumed this is on the classpath
    public static String OAPI_SRC = 'oapi.yaml'

    OpenAPI openAPI

    OapiSupport build() {
        //FUTURE allow different file to be specfied vs OAPI_SRC
        if(new ClassPathResource(OAPI_SRC).exists()) {
            openAPI = new OpenAPIV3Parser().read(OAPI_SRC)
        } else {
            log.error("Error finding $OAPI_SRC, OapiSupport not instantiated properly")
        }
        return this
    }

    // see good explanation of thread safe static instance stratgey https://stackoverflow.com/a/16106598/6500859
    @SuppressWarnings('UnusedPrivateField')
    private static class Holder {
        private static final OapiSupport INSTANCE = new OapiSupport().build();
    }

    static OapiSupport getInstance() {
        return Holder.INSTANCE
    }

    boolean hasSchema(){
        return openAPI
    }

    Schema getSchema(String name){
        return openAPI ?  openAPI.getComponents().schemas[name] : null
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
            def props = getSchema(key)
        }
    }

    static OapiSupport of(String root){
        def path = Paths.get(BuildSupport.rootProjectDir ?: '', OAPI_SRC)
        ParseOptions options = new ParseOptions()
        options.resolve = true
        options.resolveFully = true
        def sf = new OapiSupport()
        sf.openAPI = new OpenAPIV3Parser().read(path.toString(), null, options)

        return sf
    }

}
