/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileDynamic

import gorm.tools.rest.controller.RestRepoApiController
import yakworks.commons.lang.ClassUtils

@CompileDynamic
class UrlMappingsHelper {
    static String rootPath = '/api'

    static Closure getCrudMapping(String namespace, String ctrl) { { ->
        String apiPath = namespace ? "${rootPath}/${namespace}".toString() : rootPath

        group("${apiPath}/${ctrl}") {
            get "(.$format)?"(controller: ctrl, action: "list", namespace: namespace)
            get "/$id(.$format)?"(controller: ctrl, action: "get", namespace: namespace)
            get "/picklist(.$format)?"(controller: ctrl, action: "picklist", namespace: namespace)

            post "(.$format)?"(controller: ctrl, action: "post", namespace: namespace)
            put "/$id(.$format)?"(controller: ctrl, action: "put", namespace: namespace)
            patch "/$id(.$format)?"(controller: ctrl, action: "put", namespace: namespace)

            delete "/$id(.$format)?"(controller: ctrl, action: "delete", namespace: namespace)

            //BULK ops
            post "/bulk(.$format)?"(controller: ctrl, action: "bulkCreate", namespace: namespace)
            put "/bulk(.$format)?"(controller: ctrl, action: "bulkUpdate", namespace: namespace)

            //when a post is called allows an action
            post "/$action(.$format)?"(controller: ctrl, namespace: namespace)
        }
    } }

    static void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }


}
