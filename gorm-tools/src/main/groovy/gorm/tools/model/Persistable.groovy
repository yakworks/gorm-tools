/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import javax.persistence.Transient

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.springframework.lang.Nullable

import gorm.tools.utils.GormMetaUtils

/**
 * An opinionated trait implementation of Spring Data's Persistable for id and version property
 * as well as a default implementation for isNew
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.x
 */
@CompileStatic
trait Persistable<ID> { //implements IPersistable<Long>, Ident<Long> {
    //used in gorm/hibernate to check if its in the session
    abstract boolean isAttached()
    abstract Serializable getVersion()

    @Nullable
    abstract ID getId()
    abstract void setId(@Nullable ID id)

    /**
     * Returns if the {@code Persistable} is new or was persisted already.
     * The default checks if version is set and if not then its new
     *
     * @return if {@literal true} the entity is new.
     */
    @Transient
    boolean isNew(){
        // if no id then its new, if version is null then its new but version can be null
        // if its not flushed so check if its attached into the session
        return getId() == null || (getVersion() == null && !isAttached())
    }

    // ID identity() {
    //     (ID)GormMetaUtils.getId(this as GormEntity)
    // }

}
