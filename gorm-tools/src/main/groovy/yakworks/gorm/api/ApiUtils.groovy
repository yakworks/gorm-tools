/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api


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
    static Map splitPath(String pathKey){
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

}
