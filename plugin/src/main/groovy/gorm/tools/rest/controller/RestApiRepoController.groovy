/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import groovy.transform.CompileDynamic

import org.springframework.context.MessageSource

import gorm.tools.repository.GormRepoEntity
import grails.artefact.Artefact
import grails.core.GrailsApplication
import grails.util.GrailsNameUtils

/**
 * Credits: took rally.BaseDomainController with core concepts from grails RestfulConroller
 * Some of this is, especailly the cache part is lifted from the older grails2 restful-api plugin
 *
 * @author Joshua Burnett
 */
//see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
// we can get some good ideas from how that plugin does things
@SuppressWarnings(['CatchException', 'NoDef', 'ClosureAsLastMethodParameter', 'FactoryMethodName'])
@Artefact("Controller")
@CompileDynamic
class RestApiRepoController<D extends GormRepoEntity> implements RestRepositoryApi<D> {
    static allowedMethods = [list  : ["GET", "POST"], create: "POST",
                             update: ["PUT", "PATCH"], delete: "DELETE"]

    static responseFormats = ['json']
    static namespace = 'api'

    Class<D> entityClass
    String entityName
    String entityClassName
    boolean readOnly
    MessageSource messageSource

    //AppSetupService appSetupService
    GrailsApplication grailsApplication

    RestApiRepoController(Class<D> entityClass) {
        this(entityClass, false)
    }

    RestApiRepoController(Class<D> entityClass, boolean readOnly) {
        this.entityClass = entityClass
        this.readOnly = readOnly
        entityName = entityClass.simpleName
        entityClassName = GrailsNameUtils.getPropertyName(entityClass)
    }

    protected String getDomainInstanceName() {
        def suffix = grailsApplication.config?.grails?.scaffolding?.templates?.domainSuffix
        if (!suffix) {
            suffix = ''
        }
        def propName = GrailsNameUtils.getPropertyNameRepresentation(entityClass)
        "${propName}${suffix}"
    }

}
