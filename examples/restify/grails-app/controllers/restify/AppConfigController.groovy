package restify

import gorm.tools.rest.controller.RestApiController
import grails.core.GrailsApplication
import yakworks.commons.map.Maps

class AppConfigController implements RestApiController {

    GrailsApplication grailsApplication

    def get() {
        def restApiConfig = grailsApplication.config.getProperty("restApi.paths", Map)
        String pathKey = params.id
        pathKey = pathKey.replace('_','/')
        println "getting restApi key ${pathKey}"
        def resourceConfig = restApiConfig[pathKey] //grailsApplication.config.getProperty("restApi.${params.id}",Map)
        println "resourceConfig ${resourceConfig}"
        def fixedMap = Maps.removePropertyListKeys(resourceConfig)
        respond fixedMap
    }

}
