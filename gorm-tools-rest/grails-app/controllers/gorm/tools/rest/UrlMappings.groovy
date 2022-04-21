/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileDynamic

import gorm.tools.rest.controller.RestRepoApiController
import yakworks.commons.lang.ClassUtils

@CompileDynamic
class UrlMappings {
    static String rootPath = '/api'

    static mappings = {

        for (controller in getGrailsApplication().controllerClasses) {
            // println "controler $controller.fullName"
            String ctrlName = controller.logicalPropertyName
            boolean isApi = RestRepoApiController.isAssignableFrom(controller.clazz)

            if (isApi) {
                // String apiPath = namespace ? "${rootPath}/${namespace}".toString() : rootPath
                // String apiPath = namespace ? "/$namespace" : '' // ? "/api/$namespace" : "/api"
                String nspace = ClassUtils.getStaticPropertyValue(controller.clazz, 'namespace', String)
                Closure mappingClosure = UrlMappingsHelper.getCrudMapping(nspace, ctrlName)
                runClosure(mappingClosure, delegate)

                // println "delegate: $delegate"
                // group("${apiPath}/${ctrlName}") {
                //     get "(.$format)?"(controller: ctrlName, action: "list", namespace: namespace)
                //     get "/$id(.$format)?"(controller: ctrlName, action: "get", namespace: namespace)
                //     get "/picklist(.$format)?"(controller: ctrlName, action: "picklist", namespace: namespace)
                //
                //     post "/bulk(.$format)?"(controller: ctrlName, action: "bulkCreate", namespace: namespace)
                //     put "/bulk(.$format)?"(controller: ctrlName, action: "bulkUpdate", namespace: namespace)
                //
                //     post "(.$format)?"(controller: ctrlName, action: "post", namespace: namespace)
                //     put "/$id(.$format)?"(controller: ctrlName, action: "put", namespace: namespace)
                //     patch "/$id(.$format)?"(controller: ctrlName, action: "put", namespace: namespace)
                //
                //     delete "/$id(.$format)?"(controller: ctrlName, action: "delete", namespace: namespace)
                //
                //     //when a post is called allows an action
                //     post "/$action(.$format)?"(controller: ctrlName, namespace: namespace)
                //
                //     // "/schema"(controller: "schema", action: "index") {
                //     //     id = cName
                //     // }
                //
                // }
            }
        }

        // "/schema/$id?(.$format)?"(controller: "schema", action: "index")

        // "/$controller/$action?/$id?(.$format)?" {
        //     constraints {
        //         // apply constraints here
        //     }
        // }

        "/"(view: "/index")

        // the default view names are error and notFound. but grails sitemesh picks up gsps first if they exist
        // in another plugin (such as the ones that exists in spring sec and cache) and renders those gsps
        // instead of gson so for rest api its important to use unique names
        // "500"(view: '/error500')
        "404"(view: '/notFound404')
        "400"(view: '/badRequest400')
    }

    static void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }


}
