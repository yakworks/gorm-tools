/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

import org.springframework.lang.Nullable

/**
 * An opinionated trait implementation of Spring Data's Persistable for Long id and version property
 * as well as a default implementation for isNew
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.x
 */
@CompileStatic
interface IPersistable<ID> { //extends Ident<ID> {
    @Nullable
    ID getId();
    boolean isNew();
}
