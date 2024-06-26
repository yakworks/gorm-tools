/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest

import groovy.transform.CompileDynamic

import yakworks.rest.gorm.mapping.RepoApiMappingsService

@CompileDynamic
class UrlMappings {

    static mappings = {
        RepoApiMappingsService repoApiMappingsService = getApplicationContext().getBean('repoApiMappingsService', RepoApiMappingsService)
        repoApiMappingsService.createMappings(delegate)

        // "/schema/$id?(.$format)?"(controller: "schema", action: "index")

        "/"(view: "/index")

        // the default view names are error and notFound. but grails sitemesh picks up gsps first if they exist
        // in another plugin (such as the ones that exists in spring sec and cache) and renders those gsps
        // instead of gson so for rest api its important to use unique names
        // "500"(view: '/error500')
        // "404"(view: '/notFound404')
        // "400"(view: '/badRequest400')
    }

}
