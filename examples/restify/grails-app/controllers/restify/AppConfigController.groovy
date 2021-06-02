package restify

import gorm.tools.rest.RestApiConfig
import gorm.tools.rest.controller.RestApiController
import grails.core.GrailsApplication
import yakworks.commons.map.Maps

class AppConfigController implements RestApiController {

    GrailsApplication grailsApplication

    def get() {
        def resourceConfig = RestApiConfig.getPathConfig(params.id)
        def fixedMap = Maps.removePropertyListKeys(resourceConfig)
        respond fixedMap
    }

}
