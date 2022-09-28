/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.mapping

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry
import org.springframework.beans.factory.annotation.Autowired

import grails.core.GrailsApplication
import grails.core.GrailsClass
import yakworks.commons.lang.ClassUtils
import yakworks.rest.gorm.controller.RestRepoApiController

/**
 * Service that gets called from the static mapping in default UrlMappings in this plugin.
 * Can also be used to setup more and make nested child resources
 */
@SuppressWarnings('Indentation')
@CompileStatic
class RepoApiMappingsService {
    //the root/base dir for the paths TODO get this from config
    String contextPath = '/api'

    //FIXME see note in GormRestGrailsPlugin, Autowired not working. Might be being picked up to early in the game with dev.
    @Autowired ConstraintRegistry urlMappingsConstraintRegistry
    @Autowired GrailsApplication grailsApplication

    /**
     * creates the default mappings for the RestRepoApiControllers
     * @param builderDelegate the DefaultUrlMappingEvaluator.UrlMappingBuilder from mapping closuer
     */
    void createMappings(Object builderDelegate){
        if(!grailsApplication) return
        GrailsClass[] controllerClasses = grailsApplication.getArtefacts(ControllerArtefactHandler.TYPE)
        for (controller in controllerClasses) {
            // println "controler $controller.fullName"
            String ctrlName = controller.logicalPropertyName
            boolean isApi = RestRepoApiController.isAssignableFrom(controller.clazz)
            if (isApi) {
                String nspace = ClassUtils.getStaticPropertyValue(controller.clazz, 'namespace', String)
                CrudUrlMappingsBuilder.of(contextPath, nspace, ctrlName).build(builderDelegate)

                // bulks ops at /bulk
                SimpleUrlMappingBuilder.of(contextPath, nspace, ctrlName)
                    .httpMethod('POST').action('bulkCreate').suffix('/bulk')
                    .urlMappingBuilder(builderDelegate).build()
                SimpleUrlMappingBuilder.of(contextPath, nspace, ctrlName)
                    .httpMethod('PUT').action('bulkUpdate').suffix('/bulk')
                    .urlMappingBuilder(builderDelegate).build()

                //allow POST any action added to the controller
                // /api/nspace/controller/$action
                // post "/$action(.$format)?"(controller: cName)
                SimpleUrlMappingBuilder.of(contextPath, nspace, ctrlName)
                    .httpMethod('POST').suffix('/(*)').matchParams(['action'])
                    .urlMappingBuilder(builderDelegate).build()
            }
        }
    }

    @CompileDynamic
    Closure getBulkMapping(String namespace, String ctrl) { { ->
        String apiPath = namespace ? "${contextPath}/${namespace}".toString() : rootPath

        group("${apiPath}/${ctrl}") {
            //BULK ops
            post "/bulk(.$format)?"(controller: ctrl, action: "bulkCreate", namespace: namespace)
            put "/bulk(.$format)?"(controller: ctrl, action: "bulkUpdate", namespace: namespace)
        }
    } }

    /**
     * creates mapping under a resource.
     * for example passing in [namespace: rally, parentCtrl: org, parentParam: orgId, ctrl: contact]
     * would result in standard crud mappings like following
     * GET /rally/org/$orgId/contact
     * GET /rally/org/$orgId/contact/$id
     * POST /rally/org/$orgId/contact
     * etc...
     */
    void createNestedMappings(String namespace, String parentCtrl, String parentParam, String ctrl, Object builderDelegate){
        CrudUrlMappingsBuilder.of(contextPath, namespace, ctrl)
            .withParent(parentCtrl, parentParam)
            .build(builderDelegate)
    }

    void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }
}
