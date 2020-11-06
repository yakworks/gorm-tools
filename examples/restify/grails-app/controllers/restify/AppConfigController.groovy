package restify

import gorm.tools.beans.Maps
import grails.core.GrailsApplication

class AppConfigController {
    static namespace = 'api'

    GrailsApplication grailsApplication

    def get() {
        def resourceConfig = grailsApplication.config.getProperty("restApi.${params.id}",Map)
        def fixedMap = Maps.removePropertyListKeys(resourceConfig)
        respond fixedMap
    }

}
