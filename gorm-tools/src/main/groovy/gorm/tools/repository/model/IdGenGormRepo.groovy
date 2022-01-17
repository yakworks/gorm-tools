/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import javax.annotation.Resource

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

import gorm.tools.idgen.IdGenerator
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs

/**
 * A trait that adds id generator to repo for manually generating ids during validation and for persist
 * instead of waiting for hibernate to generate it. Used when associations get messy and for performance when inserting.
 * uses the default Long type from IdGenerator bean
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.3
 */
@CompileStatic
trait IdGenGormRepo<D extends Persistable> extends GormRepo<D> {

    @Resource(name="idGenerator") //easier to name with @Resource
    IdGenerator idGenerator

    String idGeneratorKey

    /**
     * calls the idGenerator.getNextId(getIdGeneratorKey())
     */
    Long generateId() {
        return idGenerator.getNextId(getIdGeneratorKey())
    }

    /**
     * if entity.id is null then generates and assigns new id to id property on entity,
     * if entity.id is already set then it just returns it
     */
    Long generateId(Persistable entity){
        if (entity.id == null) entity.id = generateId()
        return entity.id
    }

    /**
     * creates a key with getEntityClass().simpleName + .id
     */
    String getIdGeneratorKey() {
        if (!idGeneratorKey) this.idGeneratorKey = "${getEntityClass().simpleName}.id"
        return idGeneratorKey
    }

    @Override
    void doBeforePersist(D entity, PersistArgs args){
        // generate id if not already set
        generateId((Persistable)entity)
        //fire event
        getRepoEventPublisher().doBeforePersist(this, (GormEntity)entity, args)
    }

}
