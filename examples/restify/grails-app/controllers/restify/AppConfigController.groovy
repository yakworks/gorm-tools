package restify


import grails.core.GrailsApplication

class AppConfigController {
    static namespace = 'api'

    GrailsApplication grailsApplication
    def get() {
        ConfigObject resourceConfig = grailsApplication.config.restApi[params.id]
        respond resourceConfig
    }

}
