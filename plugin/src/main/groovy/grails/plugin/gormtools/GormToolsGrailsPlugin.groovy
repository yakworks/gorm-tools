package grails.plugin.gormtools

import grails.core.ArtefactHandler

class GormToolsGrailsPlugin extends grails.plugins.Plugin {
    def loadAfter = ['hibernate', 'datasources']

    def watchedResources = [
        "file:./grails-app/repository/**/*Repo.groovy",
        "file:./grails-app/services/**/*Repo.groovy",
        "file:./grails-app/domain/**/*.groovy",
        "file:./plugins/*/grails-app/repository/**/*Repo.groovy",
        "file:./plugins/*/grails-app/services/**/*Repo.groovy"
    ]

    List<ArtefactHandler> artefacts = GormToolsPluginHelper.artefacts

    Closure doWithSpring() {
        return GormToolsPluginHelper.doWithSpring
    }

    @Override
    void onChange(Map<String, Object> event) {
        GormToolsPluginHelper.onChange(event, grailsApplication, this)
    }


    @Override
    void onStartup(Map event) {
        GormToolsPluginHelper.addQuickSearchFields(config, grailsApplication)
    }

}
