/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api


import java.nio.charset.StandardCharsets

import groovy.transform.CompileStatic

/**
 * Helper statics for the API functions.
 */
@SuppressWarnings(['Println', 'ParameterCount'])
@CompileStatic
class ApiUtils {

    /**
     * pathKey should start with a /, and be in form /namespace/endpoint or /endpoint
     * @return map with name and namespace
     */
    static Map splitPath(String pathKey) {
        //1. it should start with a /, remove it if so.
        pathKey = pathKey.startsWith("/") ? pathKey.substring(1) : pathKey
        Map pathParts = [name: pathKey, namespace: '']
        if (pathKey.contains("/")) {
            List parts = pathKey.split("[/]") as List
            String name = parts.last()
            pathParts['name'] = name
            String namespace = pathKey.substring(0, pathKey.lastIndexOf('/'))
            pathParts['namespace'] = namespace
            return pathParts
        }
        return pathParts
    }

    /**
     * Parses name=xyz&size=123 query string into a map
     * @param queryString Query string
     * @return Map<String,String>
     */
    static Map parseQueryParams(String queryString) {
        if(!queryString) return [:]
        queryString.split("&")
            .collect { it.split("=") }
            .collectEntries { String[] arr ->
                String k = arr.length > 0 ? arr[0] : ""
                String v = arr.length > 1 ? arr[1] : ""
                return [decode(k), decode(v)]
            }
    }

    private static String decode(String s) {
        URLDecoder.decode(s, StandardCharsets.UTF_8.toString())
    }

}
