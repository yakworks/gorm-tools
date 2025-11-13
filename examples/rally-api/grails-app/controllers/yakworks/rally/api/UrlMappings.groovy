/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import yakworks.rest.gorm.mapping.RepoApiMappingsService

class UrlMappings {

    static mappings = {
        println "parsing rally-api UrlMappings"
        "/"(controller: 'application', action:'index')
        "/hazel-hibernate"(controller: 'application', action:'hazelHibernate')
        "/hazel-caches"(controller: 'application', action:'hazel')

        "/info/$action"(controller: 'appInfo')

        //for functional error tests
        "/security-tests/$action"(controller: 'securityTests')

        get "/rally/syncJob/${id}/data"(controller: 'syncJob', action: 'data')
        get "/rally/syncJob/${id}/payload"(controller: 'syncJob', action: 'payload')
        get "/rally/syncJob/${id}/problems"(controller: 'syncJob', action: 'problems')

        RepoApiMappingsService repoApiMappingsService = getApplicationContext().getBean('repoApiMappingsService', RepoApiMappingsService)
        //repoApiMappingsService.createMappings(delegate)

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
        "/appConfig/$nspace/$id"(controller: 'appConfig', action: 'get')
        // "/api/wtf"(controller: 'appConfig', action: 'wtf')
        // "500"(view: '/error')
        //ShiroGrailsExceptionResolver is setup to map UnauthorizedException to this
        "/forbidden"(controller: "forbidden")
        // "404"(controller: "errorProblem", action: 'notFound404')

        //to test errors and error handlers
        post "/api/rally/exceptionTest/runtimeException"(controller: 'exceptionTest', action:'runtimeException', namespace:'rally')
        post "/api/rally/exceptionTest/throwable"(controller: 'exceptionTest', action:'throwable', namespace:'rally')

        post "/rally/org/rpc"(controller: 'org', action: 'rpc')
    }

    static void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }
}
