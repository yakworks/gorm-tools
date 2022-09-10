/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.rest

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import yakworks.gorm.rest.controller.RestRepoApiController

/**
 * Meta Annotation to applied to a domain class if it is a REST resource
 * Creates a RestApiDomainController for it if it does not exist.
 * Doesn't allow a uri like @Resource so UrlMappings has to be used.
 *
 * @author Joshua Burnett
 *
 * based on Grails' @Resource annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("yakworks.gorm.rest.ast.RestApiConfigTransform")
@interface RestApiFromConfig {

    String namespace() default ""

    /**
     * @return The Controller class to generate. Can be set to null to skip the generation
     */
    Class<?> controllerTrait() default RestRepoApiController
}
