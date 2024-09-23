/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.context.MessageSource

import gorm.tools.jdbc.DbDialectService
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.validation.RepoValidatorRegistry
import grails.core.ArtefactHandler
import grails.plugins.Plugin
import yakworks.gorm.boot.GormToolsConfiguration

/**
 * @author Joshua Burnett (@basejump)
 */
@SuppressWarnings(['Indentation'])
@CompileStatic
@Slf4j
class GormToolsGrailsPlugin extends Plugin {

    def loadAfter = ['hibernate', 'datasources', 'grails-kit']

    //make sure we load before controllers as might be creating rest controllers
    def loadBefore = ['controllers']

    List<ArtefactHandler> artefacts = [new RepositoryArtefactHandler()] as List<ArtefactHandler>

    @Override
    @CompileDynamic
    Closure doWithSpring() {{ ->
        //with how we change config its only accesible in grails config
        // DbDialectService.init(config.getProperty("hibernate.dialect"))

        // gormToolsConfiguration(GormToolsConfiguration, grailsApplication)
        gormToolsConfiguration(GormToolsConfiguration)
    }}

    //This is kind of equivalent to init in bootstrap
    // @Override
    // void doWithApplicationContext() {
    //     HibernateDatastore datastore = applicationContext.getBean("hibernateDatastore", HibernateDatastore)
    //     RepoValidatorRegistry.init(datastore, applicationContext.getBean('messageSource', MessageSource))
    // }

    /**
     * Invoked in a phase where plugins can add dynamic methods. Subclasses should override
     */
    @Override
    void doWithDynamicMethods() {
        // String[] entities = grailsApplication.getMappingContext().getPersistentEntities()*.name
        // println ("entities $entities")
//        GrailsDomainBinder.class.declaredFields.each { Field f ->
//            if(f.name == 'FOREIGN_KEY_SUFFIX'){
//                println "changing FOREIGN_KEY_SUFFIX"
//                GormToolsPluginHelper.setFinalStatic(f, 'Id')
//            }
//        }
//        assert GrailsDomainBinder.FOREIGN_KEY_SUFFIX == 'Id'
    }
}
