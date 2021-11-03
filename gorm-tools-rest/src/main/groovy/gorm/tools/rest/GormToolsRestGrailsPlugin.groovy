/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileStatic

import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler

import gorm.tools.openapi.GormToSchema
import gorm.tools.openapi.OpenApiGenerator
import gorm.tools.rest.render.JsonGeneratorRenderer
import gorm.tools.rest.render.PagerRenderer
import gorm.tools.rest.render.ProblemRenderer
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsControllerClass
import grails.plugins.Plugin
import yakworks.commons.lang.NameUtils

@SuppressWarnings(['UnnecessarySelfAssignment', 'Println', 'EmptyMethod'])
class GormToolsRestGrailsPlugin extends Plugin {

    def loadAfter = ['gorm-tools']
    //make sure we load before controllers as might be creating rest controllers
    def loadBefore = ['controllers']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() {
        {->

            tomcatWebServerCustomizer(RestTomcatWebServerCustomizer)

            restApiConfig(RestApiConfig){ bean -> bean.lazyInit = true}

            //renderers
            mapJsonRenderer(JsonGeneratorRenderer, Map)
            problemRenderer(ProblemRenderer)
            pagerRenderer(PagerRenderer)

            gormToSchema(GormToSchema) { bean ->
                bean.lazyInit = true
            }

            openApiGenerator(OpenApiGenerator) { bean ->
                bean.lazyInit = true
            }

            //restApiControllersFromConfig(application)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }

    //old
    void controllersFromDomainAnnotation(){
        //controller names to be used during iterations, do it so we only itrate once
        List<GrailsControllerClass> ctrlList = getExistingControllers(application)
        //println ctrlNames

        for (GrailsClass grailsClass in application.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            final domainClass = grailsClass.clazz

            // if it has the RestApi annotation then make sure the controller that was created for it gets added
            if (domainClass.getAnnotation(RestApi)) {
                String controllerName = "${domainClass.name}Controller"
                //Check if we already have such controller in app
                addControllerWhenNotExists(application, ctrlList, controllerName)
            }
        }
    }

    /**
     * Makes sure the controllers created from a domain with the @RestApi annotation
     * are registered as controller artifacts
     */
    @CompileStatic
    void restApiControllersFromConfig(GrailsApplication app) {
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
    void addControllerWhenNotExists(GrailsApplication app, List<GrailsControllerClass> ctrlList, String className) {
        Class ctrlClass
        try {
            ctrlClass = app.classLoader.loadClass(className)
        } catch (ClassNotFoundException cnfe) {
            println "addControllerArtifact ClassNotFoundException on classLoader.loadClass($className)"
        }
        if (!controllerExists(app, ctrlList, className, ctrlClass)) {
            println "adding controller $className"
            app.addArtefact(ControllerArtefactHandler.TYPE, ctrlClass)
        }

    }

    @CompileStatic
    boolean controllerExists(GrailsApplication app, List<GrailsControllerClass> ctrlList, String controllerClassName, Class controllerClass) {
        String shortName = NameUtils.getShortName(controllerClassName)
        GrailsControllerClass ctrlClass = getController(app, controllerClassName)
        if(ctrlClass && ctrlClass.namespace?.startsWith('api')) return true

        return ctrlList.any {
            it.shortName == shortName && it.namespace.startsWith('api')
        }
    }

    List<GrailsControllerClass> getExistingControllers(GrailsApplication app){
        app.getArtefacts(ControllerArtefactHandler.TYPE).collect{ it as GrailsControllerClass}
    }

    GrailsControllerClass getController(GrailsApplication app, String controllerClassName){
        (GrailsControllerClass) app.getArtefact(ControllerArtefactHandler.TYPE, controllerClassName)
    }
}
