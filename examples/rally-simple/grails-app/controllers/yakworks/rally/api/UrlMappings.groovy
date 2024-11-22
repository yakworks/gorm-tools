/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import yakworks.rest.gorm.mapping.RepoApiMappingsService

class UrlMappings {

    static mappings = {
        println "parsing rally-simple UrlMappings"
        "/"(controller: 'application', action:'index')
        RepoApiMappingsService repoApiMappingsService = getApplicationContext().getBean('repoApiMappingsService', RepoApiMappingsService)
        repoApiMappingsService.createNestedMappings('rally', 'org', 'orgId', 'contact', delegate)
    }

    static void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }
}
