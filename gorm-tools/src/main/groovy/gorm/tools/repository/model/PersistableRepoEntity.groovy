/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.validation.ApiConstraints
import gorm.tools.utils.GormMetaUtils
import grails.gorm.validation.ConstrainedProperty

/**
 * core trait for repo methods that use the repo for persistance
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait PersistableRepoEntity<D, R extends GormRepo<D>> implements HasRepo<D, R>, Persistable {

    static R getRepo() { return (R) RepoUtil.findRepo(this) }

    R findRepo() { return (R) RepoUtil.findRepo(getClass()) }

    D persist(Map args = [:]) {
        return findRepo().persist((D) this, args)
    }

    void remove(Map args = [:]) {
        findRepo().remove((D) this, args)
    }

    void bind(Map args = [:], Map data) {
        findRepo().getEntityMapBinder().bind(args, (D) this, data)
    }

    /**
     * Creates, binds and persists and instance
     * @return The created instance
     */
    static D create(Map args = [:], Map data) {
        return getRepo().create(data, args)
    }

    static D update(Map args = [:], Map data) {
        getRepo().update(data, args)
    }

    static void removeById(Map args = [:], Serializable id) {
        getRepo().removeById(id, args)
    }

    /**
     * default constraints static that calls findConstraints(delegate)
     */
    @CompileDynamic
    static Closure getConstraints(){
        return {
            apiConstraints(getDelegate())
        }
    }

    static void apiConstraints(Object builder){
        ApiConstraints.processConstraints(this, builder)
    }

    // static ApiConstraints getApiConstraints(){
    //     ApiConstraints.findApiConstraints(this)
    // }

    /**
     * @return The constrained properties for this domain class
     */
    // @CompileDynamic //so it can access getGormPersistentEntity, FIXME look into implementing GormEntity
    // static Map<String, ConstrainedProperty> getConstrainedProperties() {
    //     GormMetaUtils.findConstrainedProperties(getGormPersistentEntity())
    // }

}
