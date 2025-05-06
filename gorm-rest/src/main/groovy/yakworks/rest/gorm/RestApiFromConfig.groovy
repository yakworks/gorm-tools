/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * Meta Annotation to be applied to Spring Confguration class.
 * Uses the restapi-config.yml for configuration and list of domains to setup.
 *
 * @author Joshua Burnett
 *
 * based on Grails' @Resource annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("yakworks.rest.gorm.ast.RestApiConfigTransform")
@interface RestApiFromConfig {

    //NOTE: THIS IS NOT USED ANYWHERE RIGHT NOW.
    String namespace() default ""

    /**
     * @return The Controller class to generate. Can be set to null to skip the generation
     * NOTE: THIS IS NOT USED ANYWHERE RIGHT NOW.
     */
    // Class<?> controllerTrait() default CrudApiController
}
