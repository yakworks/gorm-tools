/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.gormtools

import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate.datasource.MultipleDataSourceSupport
import org.hibernate.SessionFactory
import org.springframework.beans.BeanWrapperImpl

import gorm.tools.hibernate.criteria.GormHibernateCriteriaBuilder
import grails.core.ArtefactHandler
import grails.core.GrailsApplication

/**
 * @author Joshua Burnett (@basejump)
 */
@Slf4j
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

       /* Class[] domainClasses = grailsApplication.domainClasses*.clazz
        domainClasses.each { Class dc ->

            if (dc.name.startsWith('grails.plugin.')) {
                //domains from Spring Security Acl, for example, works only with default GORM behaviour
                //most likely same is for other standard domains
                return
            }

            def extension = dc.metaClass
            String datasourceName = MultipleDataSourceSupport.getDefaultDataSource(dc.gormPersistentEntity)
            boolean isDefault = (datasourceName == ConnectionSource.DEFAULT)
            String suffix = isDefault ? '' : '_' + datasourceName
            SessionFactory sessionFactory = grailsApplication.mainContext.getBean("sessionFactory$suffix")
            HibernateDatastore datastore = grailsApplication.mainContext.getBean("hibernateDatastore")

            if (!sessionFactory) {
                log.error("No session factory found for datasource $datasourceName configured in domain class $dc.simpleName")
            }

            if (!datastore) {
                log.error("No DataStore bean found for datasource $datasourceName configured in domain class $dc.simpleName")
            }
            registerCriteria(dc, extension, sessionFactory, datastore, grailsApplication)
        }*/

    }

    private void registerCriteria(Class dc, MetaClass extension, SessionFactory sessionFactory,
                                  HibernateDatastore datastore, GrailsApplication grailsApplication) {
        Class domainClassType = dc
        ['createCriteria', 'makeCriteria'].eachWithIndex { name, idx ->
            extension.static."$name" = { ->
                GormHibernateCriteriaBuilder builder = new GormHibernateCriteriaBuilder(domainClassType, sessionFactory)
                builder.conversionService = datastore.mappingContext.conversionService
                //builder.grailsApplication = grailsApplication
                return builder
            }
        }
        ['withCriteria', 'forCriteria'].each {
            extension.'static'."$it" = { Closure callable ->
                def builder = new GormHibernateCriteriaBuilder(domainClassType, sessionFactory)
                builder.conversionService = datastore.mappingContext.conversionService
                //builder.grailsApplication = grailsApplication
                builder.invokeMethod("doCall", callable)
            }
            extension.'static'."$it" = { Map builderArgs, Closure callable ->
                def builder = new GormHibernateCriteriaBuilder(domainClassType, sessionFactory)
                builder.conversionService = datastore.mappingContext.conversionService
                builder.grailsApplication = grailsApplication
                def builderBean = new BeanWrapperImpl(builder)
                for (entry in builderArgs) {
                    if (builderBean.isWritableProperty(entry.key)) {
                        builderBean.setPropertyValue(entry.key, entry.value)
                    }
                }
                builder.invokeMethod("doCall", callable)
            }
        }
    }

}
