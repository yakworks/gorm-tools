/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.DbDialectService
import gorm.tools.async.GparsAsyncSupport
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.JdbcIdGenerator
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.mango.DefaultMangoQuery
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.artefact.GrailsRepositoryClass
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.rest.JsonSchemaGenerator
import gorm.tools.rest.RestApi
import gorm.tools.rest.RestApiConfig
import gorm.tools.support.MsgService
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsControllerClass
import grails.plugins.Plugin
import grails.util.GrailsNameUtils

@SuppressWarnings(['UnnecessarySelfAssignment'])
@CompileDynamic
class GormToolsPluginHelper {
    static List<ArtefactHandler> artefacts = [new RepositoryArtefactHandler()]

    static Closure doWithSpring = {
        println "starting gorm-tools config"
        msgService(MsgService)

        restApiConfig(RestApiConfig)

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

        DbDialectService.dialectName = application.config.hibernate.dialect

        def repoClasses = application.repositoryClasses
        repoClasses.each { repoClass ->
            getRepoBeanClosure(repoClass, delegate).call()
        }


        //controller names to be used during iterations, do it so we only itrate once
        List<GrailsControllerClass> ctrlList = getExistingControllers(application)
        //println ctrlNames

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
                addControllerWhenNotExists(application, ctrlList, controllerName)
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
     * Makes sure the controllers created from a domain with the @RestApi annotation
     * are registered as controller artifacts
     * @param app
     */
    @CompileStatic
    static void restApiControllersFromConfig(GrailsApplication app) {
        //controller names to be used during iterations
        List<GrailsControllerClass> ctrlList = getExistingControllers(app)
        // println ctrlNames

        Map restApi = app.config.getProperty('restApi', Map)
        restApi.each { k, v ->
            Map entry = v as Map
            if(entry?.entityClass){
                String controllerClassName = "${entry.entityClass}Controller" //ex com.foo.FooController
                println "adding ${controllerClassName}"
                addControllerWhenNotExists(app, ctrlList, controllerClassName)
            }
        }
    }

    /**
     * calls addArtefact with loadClass on className, just swallows the error if ClassNotFoundException
     */
    @CompileStatic
    static void addControllerWhenNotExists(GrailsApplication app, List<GrailsControllerClass> ctrlList, String className) {
        Class ctrlClass
        try {
            ctrlClass = app.classLoader.loadClass(className)
        } catch (ClassNotFoundException cnfe) {
            println "addControllerArtifact ClassNotFoundException on classLoader.loadClass($className)"
        }
        if (!controllerExists(app, ctrlList, className, ctrlClass)) {
            println "adding $className"
            app.addArtefact(ControllerArtefactHandler.TYPE, ctrlClass)
        }

    }

    @CompileStatic
    static boolean controllerExists(GrailsApplication app, List<GrailsControllerClass> ctrlList, String controllerClassName, Class controllerClass) {
        String shortName = GrailsNameUtils.getShortName(controllerClassName)
        GrailsControllerClass ctrlClass = getController(app, controllerClassName)
        if(ctrlClass && ctrlClass.namespace == 'api') return true

        return ctrlList.any {
            it.shortName == shortName && it.namespace == 'api'
        }
    }

    static List<GrailsControllerClass> getExistingControllers(GrailsApplication app){
        app.getArtefacts(ControllerArtefactHandler.TYPE).collect{ it as GrailsControllerClass}
    }

    static GrailsControllerClass getController(GrailsApplication app, String controllerClassName){
        (GrailsControllerClass) app.getArtefact(ControllerArtefactHandler.TYPE, controllerClassName)
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
