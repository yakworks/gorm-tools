/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.gormtools

import grails.core.ArtefactHandler

/**
 * @author Joshua Burnett (@basejump)
 */
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
        GormToolsPluginHelper.addQuickSearchFields(
            config.gorm?.tools?.mango?.defaultQuickSearch ?: [],
            grailsApplication.getMappingContext().getPersistentEntities() as List
        )
    }

    /**
     * Invoked in a phase where plugins can add dynamic methods. Subclasses should override
     */
    @Override
    void doWithDynamicMethods() {
//        GrailsDomainBinder.class.declaredFields.each { Field f ->
//            if(f.name == 'FOREIGN_KEY_SUFFIX'){
//                println "changing FOREIGN_KEY_SUFFIX"
//                GormToolsPluginHelper.setFinalStatic(f, 'Id')
//            }
//        }
//        assert GrailsDomainBinder.FOREIGN_KEY_SUFFIX == 'Id'
    }

}
