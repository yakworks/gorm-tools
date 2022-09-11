package yakworks.rally.api

import groovy.transform.CompileStatic

import gorm.tools.api.IncludesConfig
import yakworks.commons.map.Maps
import yakworks.rest.gorm.controller.RestApiController

@CompileStatic
class AppConfigController implements RestApiController {

    IncludesConfig includesConfig

    def get() {
        String namespace = params.nspace
        String controllerKey = params.id
        def resourceConfig = includesConfig.getPathConfig(controllerKey, namespace)
        assert resourceConfig
        def fixedMap = Maps.removePropertyListKeys(resourceConfig)
        respond fixedMap
    }

}
