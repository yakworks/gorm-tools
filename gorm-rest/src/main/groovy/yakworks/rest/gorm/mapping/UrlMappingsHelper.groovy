/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.mapping

import groovy.transform.CompileDynamic

@SuppressWarnings(['Indentation'])
@CompileDynamic
class UrlMappingsHelper {
    static String rootPath = '/api'

    /**
     * bulding url maps is pretty hacky in grails and there is no good clean way to do it without builder hacking.
     */
    static Closure getCrudMapping(String namespace, String ctrl, String baseResource = null) { { ->
        String apiPathBase = namespace ? "${rootPath}/${namespace}".toString() : rootPath

        String apiPath = "${apiPathBase}/${ctrl}"

        Closure getPath = { String suffix ->
            if(!suffix) suffix =''
            return "${apiPath}${suffix}(.$format)?"
        }
        if(baseResource){
            //override the getPath for nested.
            getPath = { String suffix ->
                if(!suffix) suffix =''
                //register the var
                String entityIdWild = getProperty("${baseResource}Id")
                String nestedPath = "${apiPathBase}/${baseResource}/${entityIdWild}/${ctrl}"
                String finalDeal = "${nestedPath}${suffix}(.$format)?"
                return finalDeal
            }
        }
        getPath.delegate = delegate

        // group("${apiPath}") {
            get "${getPath()}"(controller: ctrl, action: "list", namespace: namespace) {
                if(baseResource) rootResource = baseResource
            }
            get "${getPath("/$id")}"(controller: ctrl, action: "get", namespace: namespace){
                if(baseResource) rootResource = baseResource
            }
            get "${getPath('/picklist')}"(controller: ctrl, action: "picklist", namespace: namespace){
                if(baseResource) rootResource = baseResource
            }

            post "${getPath()}"(controller: ctrl, action: "post", namespace: namespace){
                if(baseResource) rootResource = baseResource
            }
            put "${getPath("/$id")}"(controller: ctrl, action: "put", namespace: namespace) {
                if(baseResource) rootResource = baseResource
            }
            patch "${getPath("/$id")}"(controller: ctrl, action: "put", namespace: namespace) {
                if(baseResource) rootResource = baseResource
            }

            delete "${getPath("/$id")}"(controller: ctrl, action: "delete", namespace: namespace) {
                if(baseResource) rootResource = baseResource
            }

            //when a post is called allows an action
            post "${apiPath}/$action(.$format)?"(controller: ctrl, namespace: namespace) {
                rootResource = rootResource
            }

            // println delegate

    } }

    static Closure getBulkMapping(String namespace, String ctrl) { { ->
        String apiPath = namespace ? "${rootPath}/${namespace}".toString() : rootPath

        group("${apiPath}/${ctrl}") {
            //BULK ops
            post "/bulk(.$format)?"(controller: ctrl, action: "bulkCreate", namespace: namespace)
            put "/bulk(.$format)?"(controller: ctrl, action: "bulkUpdate", namespace: namespace)
        }
    } }

    static void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }


}
