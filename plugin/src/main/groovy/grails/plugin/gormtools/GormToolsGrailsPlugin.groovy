package grails.plugin.gormtools

import grails.core.ArtefactHandler

class GormToolsGrailsPlugin extends grails.plugins.Plugin {
    def loadAfter = ['hibernate', 'datasources']

    def watchedResources = [
        "file:./grails-app/repository/**/*Dao.groovy",
        "file:./grails-app/services/**/*Dao.groovy",
        "file:./grails-app/domain/**/*.groovy",
        "file:./plugins/*/grails-app/repository/**/*Dao.groovy",
        "file:./plugins/*/grails-app/services/**/*Dao.groovy"
    ]

    List<ArtefactHandler> artefacts = GormToolsPluginHelper.artefacts

    Closure doWithSpring() {
        return GormToolsPluginHelper.doWithSpring
    }

    @Override
    void onChange(Map<String, Object> event) {
        GormToolsPluginHelper.onChange(event, grailsApplication, this)
    }

}
