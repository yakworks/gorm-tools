/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest

import groovy.transform.CompileStatic

import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.gorm.validation.constraints.registry.DefaultConstraintRegistry

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsControllerClass
import grails.plugins.Plugin
import yakworks.commons.lang.NameUtils
import yakworks.rest.gorm.RestApi
import yakworks.rest.gorm.mapping.RepoApiMappingsService
import yakworks.rest.gorm.render.ApiResultsRenderer
import yakworks.rest.gorm.render.CSVPagerRenderer
import yakworks.rest.gorm.render.JsonGeneratorRenderer
import yakworks.rest.gorm.render.PagerRenderer
import yakworks.rest.gorm.render.ProblemRenderer
import yakworks.rest.gorm.render.SyncJobRenderer
import yakworks.rest.gorm.render.XlsxPagerRenderer

@SuppressWarnings(['UnnecessarySelfAssignment', 'Println', 'EmptyMethod', 'Indentation'])
class GormRestGrailsPlugin extends Plugin {

    def loadAfter = ['gorm-openapi']
    //make sure we load before controllers as might be creating rest controllers
    def loadBefore = ['controllers']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() { {->

        tomcatWebServerCustomizer(RestTomcatWebServerCustomizer)
        //setup to try and speed up constraint eval so its only setup once.
        urlMappingsConstraintRegistry(DefaultConstraintRegistry, ref('messageSource'))
        //the default UrlMappings calls this.
        repoApiMappingsService(RepoApiMappingsService){
            // FIXME @Autowired is not working on RepoApiMappingsService during dev, works when run from tests or in prod.
            // doing this for now until we sort out why.
            grailsApplication = ref('grailsApplication')
            urlMappingsConstraintRegistry = ref('urlMappingsConstraintRegistry')
        }
        //renderers
        mapJsonRenderer(JsonGeneratorRenderer, Map)
        apiResultsRenderer(ApiResultsRenderer)
        problemRenderer(ProblemRenderer)
        pagerRenderer(PagerRenderer)
        syncJobRenderer(SyncJobRenderer)


        csvPagerRenderer(CSVPagerRenderer)
        xlsxPagerRenderer(XlsxPagerRenderer)

    } }

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

        Map restApi = app.config.getProperty('api', Map)
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
