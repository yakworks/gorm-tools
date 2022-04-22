/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.mapping

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.rest.controller.RestRepoApiController
import grails.core.GrailsApplication
import grails.core.GrailsClass
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.UrlMappingsArtefactHandler
import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry
import org.springframework.beans.factory.annotation.Autowired
import yakworks.commons.lang.ClassUtils

/**
 * Service that gets called from the static mapping in default UrlMappings in this plugin.
 * Can also be used to setup more and make nested child resources
 */
@CompileStatic
class RepoApiMappingsService {
    //the root/base dir for the paths TODO get this from config
    String contextPath = '/api'

    @Autowired ConstraintRegistry urlMappingsConstraintRegistry
    @Autowired GrailsApplication grailsApplication

    /**
     * creates the default mappings for the RestRepoApiControllers
     * @param builderDelegate the DefaultUrlMappingEvaluator.UrlMappingBuilder from mapping closuer
     */
    void createMappings(Object builderDelegate){
        GrailsClass[] controllerClasses = grailsApplication.getArtefacts(ControllerArtefactHandler.TYPE)
        for (controller in controllerClasses) {
            // println "controler $controller.fullName"
            String ctrlName = controller.logicalPropertyName
            boolean isApi = RestRepoApiController.isAssignableFrom(controller.clazz)
            if (isApi) {
                String nspace = ClassUtils.getStaticPropertyValue(controller.clazz, 'namespace', String)
                CrudUrlMappingsBuilder.of(contextPath, nspace, ctrlName).build(builderDelegate)
                // Closure mappingClosure = UrlMappingsHelper.getCrudMapping(nspace, ctrlName)
                // runClosure(mappingClosure, delegate)
                Closure bulkMappingClosure = getBulkMapping(nspace, ctrlName)
                runClosure(bulkMappingClosure, builderDelegate)
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

    void runClosure(Closure mappingClosure, Object delegate) {
        mappingClosure.delegate = delegate
        mappingClosure()
    }
}
