/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.json

import javax.servlet.http.HttpServletRequest

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import yakworks.commons.json.JsonEngine

/**
 * Trait to adds parse methods for using JsonSlurper to parse HttpServletRequest body
 *
 * @author Joshua Burnett
 * @since 7.0.8
 */
@CompileStatic
trait JsonParserTrait {

    JsonSlurper getJsonSlurper(){
        return JsonEngine.slurper
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

    Object parseJsonBytes(byte[] bytes){
        // TODO not sure whats up here but these stops some of the null issues
        parseJsonText(new String(bytes, "UTF-8"))
        // return getJsonSlurper().parse(bytes)
    }

    Object parseJsonText(String text){
        return getJsonSlurper().parseText(text)
    }

}
