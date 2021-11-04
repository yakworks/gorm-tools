/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json

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
trait HttpJsonParserTrait extends JsonEngineTrait{

    /**
     * Parse a JSON Map data structure from request body and casts.
     * Clazz should be either list or map
     */
    public <T> T parseJson(HttpServletRequest req, Class<T> clazz) {
        boolean hasContent = req.contentLength
        Object parsedObj = parseJson(req)
        //if its not a map type then assume its list
        if(!hasContent && !Map.isAssignableFrom(clazz)){
            parsedObj = Collections.emptyList()
        }
        JsonEngine.validateExpectedClass(clazz, parsedObj)
        return (T)parsedObj
    }

    /**
     * Parse a JSON Map data structure from request body input stream.
     * if no content then returns an empty map
     */
    Object parseJson(HttpServletRequest req) {
        boolean hasContent = req.contentLength
        return hasContent ? getJsonSlurper().parse(req.inputStream) : Collections.emptyMap()
    }

}
