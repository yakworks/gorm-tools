/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileStatic

import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler

import gorm.tools.rest.appinfo.AppInfoBuilder
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin

@SuppressWarnings(['NoDef', 'EmptyMethod', 'VariableName', 'EmptyCatchBlock'])
class GormRestApiGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    String grailsVersion = "3.2.11 > *"
    // resources that are excluded from plugin packaging
    List pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    String title = "Gorm Rest Api Tools"
    // Headline display name of the plugin
    String author = "Your name"
    String authorEmail = ""
    String description = '''\
Brief summary/description of the plugin.
'''
    //List profiles = ['web']
    List loadBefore = ['controllers']
    List observe = ['domainClass']

    // URL to the plugin's documentation
    String documentation = "http://grails.org/plugin/gorm-rest-tools"

    GrailsApplication grailsApplication

    Closure doWithSpring() {
        { ->

            jsonSchemaGenerator(JsonSchemaGenerator) { bean ->
                // Autowiring behaviour. The other option is 'byType'. <<autowire>>
                // bean.autowire = 'byName'
            }

            appInfoBuilder(AppInfoBuilder) { bean ->
                // Autowiring behaviour. The other option is 'byType'. <<autowire>>
                // bean.autowire = 'byName'
            }

            GrailsApplication application = grailsApplication
            GormRestApiGrailsPlugin.registryRestApiControllers(application)

        }
    }

    @Override
    void onChange(Map<String, Object> event) {
        GormRestApiGrailsPlugin.registryRestApiControllers(grailsApplication)
    }

    @CompileStatic
    static void registryRestApiControllers(GrailsApplication app) {
        for (GrailsClass grailsClass in app.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            final clazz = grailsClass.clazz
            if (clazz.getAnnotation(RestApi)) {
                //println "${clazz.name}"
                String controllerClassName = "${clazz.name}Controller"
                //Check if we already have such controller in app
                if (!app.getArtefact(ControllerArtefactHandler.TYPE, controllerClassName) && !(app.getArtefacts
                (ControllerArtefactHandler.TYPE)*.name.contains(clazz.simpleName))) {

                    try {
                        app.addArtefact(ControllerArtefactHandler.TYPE, app.classLoader.loadClass(controllerClassName))
                        //println "added $controllerClassName"
                    } catch (ClassNotFoundException cnfe) {

                    }
                }
            }
        }
    }

}
