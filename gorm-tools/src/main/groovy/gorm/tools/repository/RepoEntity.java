/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a domain entity to be injected with RepoEntity trait
 * scanned in the RepoEntityTraitInjector
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RepoEntity {
}
