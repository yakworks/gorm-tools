/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import gorm.tools.rest.mapping.RepoApiMappingsService
import gorm.tools.rest.mapping.UrlMappingsHelper

class UrlMappings {

    static mappings = {
        println "parsing restify UrlMappings"
        "/"(controller: 'application', action:'index')

        RepoApiMappingsService repoApiMappingsService = getApplicationContext().getBean('repoApiMappingsService', RepoApiMappingsService)
        // repoApiMappingsService.createMappings(delegate)

        //creates nested /rally/org/$orgId/contact....
        repoApiMappingsService.createNestedMappings('rally', 'org', 'orgId', 'contact', delegate)

        // Closure mappingClosure = UrlMappingsHelper.getCrudMapping('rally', 'contact', 'org')
        // runClosure(mappingClosure, delegate)

        // get "/api/rally/org/${getProperty('orgId')}/contact"(controller: "contact", action: "list", namespace: 'rally'){
        //     rootResource = 'org'
        // }
        // get "/api/rally/org/$orgId/contact/$id"(controller: "contact", action: "get", namespace: 'rally'){
        //     rootResource = 'org'
        // }
        // "/api/rally/foo"(resources:'org', namespace: 'rally') {
        //     "/bar"(resources:"contact", namespace: 'rally')
        // }

        "500"(view: '/error')
        //ShiroGrailsExceptionResolver is setup to map UnauthorizedException to this
        "/forbidden"(controller: "forbidden")
    }

    static void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }
}
