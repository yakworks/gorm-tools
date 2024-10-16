/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import yakworks.gorm.api.ApiConfig
import yakworks.rest.gorm.controller.RestApiController
import yakworks.security.user.CurrentUser

@CompileStatic
class AppConfigController implements RestApiController {

    ApiConfig apiConfig
    CurrentUser currentUser

    def get() {
        String namespace = params.nspace
        String controllerKey = params.id
        Map resourceConfig = apiConfig.getPathMap(controllerKey, namespace)
        assert resourceConfig
        respondWith resourceConfig
    }

}
