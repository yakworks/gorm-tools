package restify

import gorm.tools.rest.RestApiConfig
import gorm.tools.rest.controller.RestApiController
import grails.core.GrailsApplication
import yakworks.commons.map.Maps

class AppConfigController implements RestApiController {

    RestApiConfig restApiConfig

    def get() {
        def resourceConfig = restApiConfig.getPathConfig(params.id as String)
        assert resourceConfig
        def fixedMap = Maps.removePropertyListKeys(resourceConfig)
        respond fixedMap
    }

}
