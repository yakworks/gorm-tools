/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.repository;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to marks a Repository artifact when not under grails-app/repo
 * AST tranform will also add the Spring @Component annotation so that it gets picked up on a scan
 * as well as the @Artefact that grails uses and needs for artifacts.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("gorm.tools.repository.ast.GormRepositoryArtefactTypeTransformation")
public @interface GormRepository {
}
