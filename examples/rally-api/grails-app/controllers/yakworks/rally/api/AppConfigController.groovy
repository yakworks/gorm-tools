package yakworks.rally.api

import groovy.transform.CompileStatic

import yakworks.gorm.api.ApiConfig
import yakworks.rest.gorm.controller.RestApiController

@CompileStatic
class AppConfigController implements RestApiController {

    ApiConfig apiConfig

    def get() {
        String namespace = params.nspace
        String controllerKey = params.id
        Map resourceConfig = apiConfig.getPathMap(controllerKey, namespace)
        assert resourceConfig
        respond resourceConfig
    }

}
