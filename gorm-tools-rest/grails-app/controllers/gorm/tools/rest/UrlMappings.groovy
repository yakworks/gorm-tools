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
                group(apiPath) {
                    println "apiPath: $apiPath controller: $cName with namespace: $namespace"

                    "/${cName}/schema"(controller: "schema", action: "index") {
                        id = cName
                    }
                    //when a post is called allows an action
                    post "/${cName}/$action(.$format)?"(controller: cName)
                    //or
                    post "/${cName}/actions/$action(.$format)?"(controller: cName)

                    delete "/${cName}/$id(.$format)?"(controller: cName, action: "delete")
                    get "/${cName}(.$format)?"(controller: cName, action: "list")
                    get "/${cName}/$id(.$format)?"(controller: cName, action: "get")

                    //get "/${cName}/list(.$format)?"(controller: cName, action: "list")
                    get "/${cName}/picklist(.$format)?"(controller: cName, action: "picklist")
                    //post "/${cName}/list(.$format)?"(controller: cName, action: "listPost")

                    post "/${cName}(.$format)?"(controller: cName, action: "post")
                    put "/${cName}/$id(.$format)?"(controller: cName, action: "put")
                    patch "/${cName}/$id(.$format)?"(controller: cName, action: "put")
                }
            }
        }

        // group("/api") {
        //     delete "/$controller/$id(.$format)?"(action: "delete")
        //     get "/$controller(.$format)?"(action: "index")
        //     get "/$controller/$id(.$format)?"(action: "show")
        //     post "/$controller(.$format)?"(action: "save")
        //     put "/$controller/$id(.$format)?"(action: "update")
        //     patch "/$controller/$id(.$format)?"(action: "patch")
        // }

        "/schema/$id?(.$format)?"(controller: "schema", action: "index")

        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(view: "/index")

        // the default view names are error and notFound. but grails sitemesh picks them up first if they exist
        // in another plugin (such as the ones in spring sec and cache) and renders those gsps
        // instead of gson so for rest api its important to use unique names
        "500"(view: '/error500')
        "404"(view: '/notFound404')
        "400"(view: '/badRequest400')
    }
}
