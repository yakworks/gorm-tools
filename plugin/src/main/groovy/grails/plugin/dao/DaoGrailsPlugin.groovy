package grails.plugin.dao

import grails.core.ArtefactHandler

class DaoGrailsPlugin extends grails.plugins.Plugin {
    def loadAfter = ['hibernate', 'datasources']

    def watchedResources = [
        "file:./grails-app/dao/**/*Dao.groovy",
        "file:./grails-app/services/**/*Dao.groovy",
        "file:./grails-app/domain/**/*.groovy",
        "file:./plugins/*/grails-app/dao/**/*Dao.groovy",
        "file:./plugins/*/grails-app/services/**/*Dao.groovy"
    ]

    List<ArtefactHandler> artefacts = DaoPluginHelper.artefacts

    Closure doWithSpring() {
        return DaoPluginHelper.doWithSpring
    }

    @Override
    void onChange(Map<String, Object> event) {
        DaoPluginHelper.onChange(event, grailsApplication, this)
    }

    @Override
    void doWithDynamicMethods() {
        grailsApplication.domainClasses.each { domainClass ->
            List<String> fields = config.gorm?.tools?.mango?.defaultQuickSearch
            if (!domainClass.clazz.quickSearchFields && fields instanceof List){
                domainClass.clazz.quickSearchFields = fields.findAll{ //TODO: refactor
                    try {
                        domainClass.getPropertyByName(it)
                        true
                    } catch (e){
                        false
                    }
                }

            }}
    }

}
