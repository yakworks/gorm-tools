package restify

import grails.core.GrailsApplication
import yakworks.commons.map.Maps

class AppConfigController {
    static namespace = 'api'

    GrailsApplication grailsApplication

    def get() {
        def resourceConfig = grailsApplication.config.getProperty("restApi.${params.id}",Map)
        def fixedMap = Maps.removePropertyListKeys(resourceConfig)
        respond fixedMap
    }

}
