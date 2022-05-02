/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model


import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.RepoEventPublisher

/**
 * A trait that adds id generator to repo for manually generating ids during validation and for persist
 * instead of waiting for hibernate to generate it. Used when associations get messy and for performance when inserting.
 * uses the default Long type from IdGenerator bean
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.3
 */
@CompileStatic
trait UuidGeneratorRepo<D> implements GenerateId<UUID> {

    @Autowired RepoEventPublisher repoEventPublisher

    // should be implemented by GormRepo
    abstract Class<D> getEntityClass()
    abstract void doBeforePersistWithData(D entity, PersistArgs args)

    /**
     * calls the idGenerator.getNextId(getIdGeneratorKey())
     */
    UUID generateId() {
        return UUID.randomUUID()
    }

    /**
     * replace the one in gormRepo
     */
    void doBeforePersist(D entity, PersistArgs args){
        generateId((Persistable<UUID>)entity)
        if (args.bindAction && args.data){
            doBeforePersistWithData(entity, args)
        }
        getRepoEventPublisher().doBeforePersist((GormRepo)this, (GormEntity)entity, args)
    }

}
