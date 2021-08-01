/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileDynamic

import gorm.tools.rest.controller.RestApiController
import gorm.tools.rest.controller.RestRepositoryApi
import yakworks.commons.lang.ClassUtils

@CompileDynamic
class UrlMappings {

    static mappings = {

        for (controller in getGrailsApplication().controllerClasses) {
            // println "controler $controller.fullName"
            String cName = controller.logicalPropertyName
            boolean isApi = RestRepositoryApi.isAssignableFrom(controller.clazz)
            String namespace = ClassUtils.getStaticPropertyValue(controller.clazz, 'namespace', String)
           // println "controller $cName with namespace $namespace"

            if (isApi) {
                String apiPath = namespace ? "/api/$namespace" : "/api"
                // println "apiPath: $apiPath controller: $cName"
                group("${apiPath}/${cName}") {
                    get "(.$format)?"(controller: cName, action: "list")
                    get "/$id(.$format)?"(controller: cName, action: "get")
                    get "/picklist(.$format)?"(controller: cName, action: "picklist")

                    post "(.$format)?"(controller: cName, action: "post")
                    put "/$id(.$format)?"(controller: cName, action: "put")
                    patch "/$id(.$format)?"(controller: cName, action: "put")

                    delete "/$id(.$format)?"(controller: cName, action: "delete")

                    //when a post is called allows an action
                    post "/$action(.$format)?"(controller: cName)

                    "/schema"(controller: "schema", action: "index") {
                        id = cName
                    }

                }
            }
        }

        "/schema/$id?(.$format)?"(controller: "schema", action: "index")

        // "/$controller/$action?/$id?(.$format)?" {
        //     constraints {
        //         // apply constraints here
        //     }
        // }

        "/"(view: "/index")

        // the default view names are error and notFound. but grails sitemesh picks up gsps first if they exist
        // in another plugin (such as the ones that exists in spring sec and cache) and renders those gsps
        // instead of gson so for rest api its important to use unique names
        "500"(view: '/error500')
        "404"(view: '/notFound404')
        "400"(view: '/badRequest400')
    }
}
