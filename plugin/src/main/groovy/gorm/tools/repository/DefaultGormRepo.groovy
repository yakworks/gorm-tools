/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.repository

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.springframework.core.GenericTypeResolver

@CompileStatic
@Transactional
class DefaultGormRepo<D extends GormEntity> implements GormRepo<D> {

    DefaultGormRepo() {
        this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo)
    }

    DefaultGormRepo(Class<D> clazz) {
        this.entityClass = clazz
    }
}
