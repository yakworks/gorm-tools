/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

class UrlMappings {

    static mappings = {
        println "parsing restify UrlMappings"
        "/"(controller: 'application', action:'index')

        get "/api/rally/org/$orgId/contact"(controller: "contact", action: "list", namespace: 'rally'){
            rootResource = 'org'
        }
        get "/api/rally/org/$orgId/contact/$id"(controller: "contact", action: "get", namespace: 'rally'){
            rootResource = 'org'
        }

        "500"(view: '/error')
        //ShiroGrailsExceptionResolver is setup to map UnauthorizedException to this
        "/forbidden"(controller: "forbidden")
    }
}
