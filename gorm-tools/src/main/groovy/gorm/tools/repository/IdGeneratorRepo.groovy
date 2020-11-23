/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import javax.annotation.Resource

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

import gorm.tools.idgen.IdGenerator

/**
 * A trait that adds id generator to repo for manually generating ids during validation and for persist
 * instead of waiting for hibernate to generate it. Used when associations get messy and for performance when inserting.
 * uses the default Long type from IdGenerator bean
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.3
 */
@CompileStatic
trait IdGeneratorRepo {

    @Resource(name="idGenerator")
    IdGenerator idGenerator

    String idGeneratorKey

    // should be implemented by GormRepo
    abstract Class getEntityClass()

    Long generateId() {
        return idGenerator.getNextId(getIdGeneratorKey())
    }

    /**
     * generates and assigns id to id property to entity
     * @param entity
     */
    Long generateId(GormEntity entity){
        if (!entity['id']) entity['id'] = generateId()
        return entity['id'] as Long
    }

    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    String getIdGeneratorKey() {
        if (!idGeneratorKey) this.idGeneratorKey = "${getEntityClass().simpleName}.id"
        return idGeneratorKey
    }

}
