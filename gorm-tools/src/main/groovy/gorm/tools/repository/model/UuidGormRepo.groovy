/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import org.springframework.core.GenericTypeResolver

import com.github.f4b6a3.uuid.UuidCreator
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo

/**
 * Default implementation of repo that uses time ordered UUID for ids.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class UuidGormRepo<D> implements GormRepo<D> { //, GenerateId<UUID>  {

    UuidGormRepo() {
        this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo)
    }

    UuidGormRepo(Class<D> clazz) {
        setEntityClass(clazz)
    }

    UUID generateId() {
        // return UUID.randomUUID()
        return UuidCreator.getTimeOrderedWithRandom()
    }

    @Override
    Serializable generateId(Persistable entity){
        if (entity.getId() == null)
            entity.setId(generateId())
        return entity.getId() as Serializable
    }

}
