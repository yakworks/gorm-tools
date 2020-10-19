/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.springframework.core.GenericTypeResolver

import grails.gorm.transactions.Transactional

/**
 * A concrete implementation of the GormRepo
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
class DefaultGormRepo<D> implements GormRepo<D> {

    DefaultGormRepo() {
        this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo)
    }

    DefaultGormRepo(Class<D> clazz) {
        this.entityClass = clazz
    }
}
