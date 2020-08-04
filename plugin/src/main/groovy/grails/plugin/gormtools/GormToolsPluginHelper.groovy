/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.gormtools

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.DbDialectService
import gorm.tools.GormMetaUtils
import gorm.tools.async.GparsAsyncSupport
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.JdbcIdGenerator
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.mango.DefaultMangoQuery
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.rest.JsonSchemaGenerator
import gorm.tools.rest.RestApi
import gorm.tools.rest.appinfo.AppInfoBuilder
import gorm.tools.support.MsgService
import grails.core.ArtefactHandler
import grails.core.ArtefactInfo
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin
import grails.util.GrailsNameUtils

@SuppressWarnings(['UnnecessarySelfAssignment'])
@CompileDynamic
class GormToolsPluginHelper {
    static List<ArtefactHandler> artefacts = [new RepositoryArtefactHandler()]

    static Closure doWithSpring = {
        println "starting gorm-tools config"
        msgService(MsgService)

        jdbcTemplate(JdbcTemplate, ref("dataSource"))

        jdbcIdGenerator(JdbcIdGenerator) {
            jdbcTemplate = jdbcTemplate
            table = "NewObjectId"
            keyColumn = "KeyName"
            idColumn = "NextId"
        }

        idGenerator(PooledIdGenerator, jdbcIdGenerator)

        mangoQuery(DefaultMangoQuery)

        entityMapBinder(EntityMapBinder, ref('grailsApplication'))

        repoEventPublisher(RepoEventPublisher)

        repoExceptionSupport(RepoExceptionSupport)

        asyncSupport(GparsAsyncSupport)
//            { bean ->
//            bean.autowire = true
//            bean.lazyInit = true
//        }

        DbDialectService.dialectName = application.config.hibernate.dialect

        def repoClasses = application.repositoryClasses
        repoClasses.each { repoClass ->
            getRepoBeanClosure(repoClass, delegate).call()
        }


        //make sure each domain has a repository, if not set up a DefaultGormRepo for it.
        Class[] domainClasses = application.domainClasses*.clazz
        // println ("domainClasses $domainClasses")

        domainClasses.each { Class domainClass ->
            String repoName = RepoUtil.getRepoBeanName(domainClass)
            def hasRepo = repoClasses.find { it.propertyName == repoName }
            if (!hasRepo) {
                "${repoName}"(DefaultGormRepo, domainClass) { bean ->
                    bean.autowire = true
                    bean.lazyInit = true
                }
            }
        }

        //controller names to be used during iterations
        List ctrlNames = application.getArtefacts(ControllerArtefactHandler.TYPE)*.shortName
        println ctrlNames

        for (GrailsClass grailsClass in application.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            final domainClass = grailsClass.clazz

            // make sure each domain has a repository, if not set up a DefaultGormRepo for it.
            String repoName = RepoUtil.getRepoBeanName(domainClass)
            def hasRepo = repoClasses.find { it.propertyName == repoName }
            if (!hasRepo) {
                "${repoName}"(DefaultGormRepo, domainClass) { bean ->
                    bean.autowire = true
                    bean.lazyInit = true
                }
            }
            // if it has the RestApi annotation then make sure the controller that was created for it gets added
            if (domainClass.getAnnotation(RestApi)) {
                String controllerName = "${domainClass.name}Controller"
                //Check if we already have such controller in app
                addControllerWhenNotExists(application, ctrlNames, controllerName)
            }
        }

        jsonSchemaGenerator(JsonSchemaGenerator) { bean ->
            // Autowiring behaviour. The other option is 'byType'. <<autowire>>
            // bean.autowire = 'byName'
        }

//        appInfoBuilder(AppInfoBuilder) { bean ->
//            // Autowiring behaviour. The other option is 'byType'. <<autowire>>
//            // bean.autowire = 'byName'
//        }

        // application = grailsApplication
        // restApiControllersFromConfig(application)
        println "finished gorm-tools config"
    }

    static void onChange(Object event, GrailsApplication grailsApplication, Plugin plugin) {
        if (!event.source || !event.ctx) {
            return
        }
        if (grailsApplication.isArtefactOfType(RepositoryArtefactHandler.TYPE, event.source)) {

            GrailsClass repoClass = grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, event.source)

            plugin.beans(getRepoBeanClosure(repoClass))
        }
        //registryRestApiControllers(grailsApplication)
    }

    static Closure getRepoBeanClosure(GrailsRepositoryClass repoClass, Object beanBuilder = null) {
        def lazyInit = repoClass.hasProperty("lazyInit") ? repoClass.getPropertyValue("lazyInit") : true

        def bClosure = {
            "${repoClass.propertyName}"(repoClass.getClazz()) { bean ->
                bean.autowire = true
                bean.lazyInit = lazyInit
            }
        }
        if (beanBuilder) bClosure.delegate = beanBuilder

        return bClosure
    }

    /**
     * Adds quickSearch fields to domains from config, if domain has such properties.
     * Supports paths for nested domains, for example "address.city", so if domain has
     * association address and it has property city it will be added
     *
     * @param config config bean
     * @param grailsApplication grails application context
     */
    static void addQuickSearchFields(List<String> fields, List<PersistentEntity> domains){
        domains.each { domainClass ->
            if (fields && !domainClass.getJavaClass().quickSearchFields) {
                domainClass.getJavaClass().quickSearchFields = fields.findAll {
                    GormMetaUtils.hasProperty(domainClass, it as String)
                }
            }
        }
    }

    /**
     * Makes sure the controllers created from a domain with the @RestApi annotation
     * are registered as controller artifacts
     * @param app
     */
    @CompileStatic
    static void restApiControllersFromConfig(GrailsApplication app) {
        //controller names to be used during iterations
        List ctrlNames = app.getArtefacts(ControllerArtefactHandler.TYPE)*.shortName
        println ctrlNames

        Map restApi = app.config.getProperty('restApi', Map)
        restApi.each { k, v ->
            Map entry = v as Map
            if(entry?.entityClass){
                String controllerClassName = "${entry.entityClass}Controller" //ex com.foo.FooController
                println "adding ${controllerClassName}"
                addControllerWhenNotExists(app, ctrlNames, controllerClassName)
            }
        }
    }

    /**
     * calls addArtefact with loadClass on className, just swallows the error if ClassNotFoundException
     */
    @CompileStatic
    static void addControllerWhenNotExists(GrailsApplication app, List ctrlNames, String className) {
        if (!controllerExists(app, ctrlNames, className)) {
            println "adding $className"
            try {
                app.addArtefact(ControllerArtefactHandler.TYPE, app.classLoader.loadClass(className))
                //println "added $controllerClassName"
            } catch (ClassNotFoundException cnfe) {
                println "addControllerArtifact ClassNotFoundException on classLoader.loadClass($className)"
            }
        }

    }

    @CompileStatic
    static boolean controllerExists(GrailsApplication app, List ctrlNames, String controllerClassName) {
        String shortName = GrailsNameUtils.getShortName(controllerClassName)
        app.getArtefact(ControllerArtefactHandler.TYPE, controllerClassName) || ctrlNames.contains(shortName)
    }



//    static void setFinalStatic(Field field, Object newValue) throws Exception {
//        field.setAccessible(true);
//
//        // remove final modifier from field
//        Field modifiersField = Field.class.getDeclaredField("modifiers");
//        modifiersField.setAccessible(true);
//        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//
//        field.set(null, newValue);
//    }

}
