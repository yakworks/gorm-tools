/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import gorm.tools.rest.controller.RestApiRepoController

/**
 *
 * Meta Annotation to applied to a domain class if it is a REST resource
 * Creates a RestApiDomainController for it if it does not exist.
 * Doesn't allow a uri like @Resource so UrlMappings has to be used.
 *
 *
 * @author Joshua Burnett
 *
 * based on Grails' @Resource annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("gorm.tools.rest.transform.RestApiTransform")
public @interface RestApi {

    /**
     * @return The desicription of this resource. Can be used for OpenApi or other rest docs
     */
    String description() default ""

    /**
     * @return if this annotation is just for docs and you don't want a controller generated
     * then set this to false. This also used in docs and json-schema generation to find "entry points"
     * ie, is this is true then it will create it own json-schema file instead of putting in definitions
     */
    boolean endpoint() default true

    /**
     * @return Whether this is a read-only resource (one that doesn't allow DELETE, POST, PUT or PATCH requests)
     */
    boolean readOnly() default false

    /**
     * @return The allowed response formats
     */
    String[] formats() default ["json", 'xml']

    /**
     * @return The URI of the resource. If specified a {@link grails.web.mapping.UrlMapping}
     * will automatically be registered for the resource
     */
    String uri() default ""

    /**
     * @return The namespace of the resource. If specified a {@link grails.web.mapping.UrlMapping}
     * with this namespace will automatically be registered for the resource
     */
    String namespace() default ""

    /**
     * @return The Controller class to generate. Can be set to null to skip the generation
     */
    Class<?> controllerClass() default RestApiRepoController
}
