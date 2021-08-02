package restify

import gorm.tools.rest.RestApiConfig
import gorm.tools.rest.controller.RestApiController
import grails.core.GrailsApplication
import yakworks.commons.map.Maps

class AppConfigController implements RestApiController {

    RestApiConfig restApiConfig

    def get() {
        String namespace = params.nspace
        String controllerKey = params.id
        def resourceConfig = restApiConfig.getPathConfig(controllerKey, namespace)
        assert resourceConfig
        def fixedMap = Maps.removePropertyListKeys(resourceConfig)
        respond fixedMap
    }

}
