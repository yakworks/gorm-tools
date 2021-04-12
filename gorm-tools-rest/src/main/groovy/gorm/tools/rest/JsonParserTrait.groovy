/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import javax.servlet.http.HttpServletRequest

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

/**
 * Trait to adds parse methods for using JsonSlurper to parse HttpServletRequest body
 *
 * @author Joshua Burnett
 * @since 7.0.8
 */
@CompileStatic
trait JsonParserTrait {

    JsonSlurper slurperInstance //lazy, use getJsonSlurper so it can be overriden

    /**
     * lazy accesor for a JsonSlurper, use getJsonSlurper() in a trait so that this can
     * be overriden. Default is JsonParserType.LAX but may be INDEX_OVERLAY, dont use default
     */
    JsonSlurper getJsonSlurper(){
        if(!slurperInstance) slurperInstance = new JsonSlurper()//.setType(JsonParserType.LAX)
            //.setLazyChop(false).setChop(true)
        return slurperInstance
    }

    /**
     * Parse a JSON Map data structure from request body and casts to Map<String, Object>.
     * returns an empty Map if its any other object, use parseJsonList if a list is expected
     *
     * @param req the request with the json body
     * @return a Map data structure
     */
    Map<String, Object> parseJson(HttpServletRequest req) {
        final jsonObj = parseJsonObject(req)
        return ( jsonObj instanceof Map ? jsonObj : Collections.emptyMap() ) as Map<String, Object>
    }

    /**
     * parses json from request body and casts to a List
     * returns an empty list if its any other object such as a Map.
     *
     * @param req the request with the json body
     * @return a List data structure
     */
    List parseJsonList(HttpServletRequest req) {
        final jsonObj = parseJsonObject(req)
        return jsonObj instanceof List ? jsonObj : Collections.emptyList()
    }

    /**
     * Parse a JSON data structure from request body
     *
     * @param req the request with the json body
     * @return a data structure of lists and maps
     */
    Object parseJsonObject(HttpServletRequest req) {
        //|| !ignoredRequestBodyMethods.contains(method)) {
        if (req.contentLength == 0) {
            return Collections.emptyMap()
        }
        String charsetName = req.characterEncoding ?: 'UTF-8'
        Reader reader = new InputStreamReader(req.inputStream, charsetName)
        return parseJson(reader)
    }

    /**
     * Parse a JSON data structure from content from a reader
     *
     * @param reader reader over a JSON content
     * @return a data structure of lists and maps
     */
    Object parseJson(Reader reader){
        return getJsonSlurper().parse(reader)
    }

    /**
     * The Map object that can be bound to create or update domain entity.  Defaults whats in the request based on mime-type.
     * Subclasses may override this
     */
    // @CompileDynamic //so it can access the SimpleMapDataBindingSource.map
    // Map getDataMap() {
    //     SimpleMapDataBindingSource bsrc =
    //         (SimpleMapDataBindingSource) DataBindingUtils.createDataBindingSource(grailsApplication, getEntityClass(), getRequest())
    //     return bsrc.map
    // }

}
