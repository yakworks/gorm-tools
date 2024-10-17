/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.repository;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Service annotation to make a GormRepo.
 * Just adds the @Component
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
//@GroovyASTTransformationClass("gorm.tools.repository.ast.GormRepositoryArtefactTypeTransformation")
public @interface GormRepository {
}
