/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoLookup
import gorm.tools.validation.ApiConstraints

/**
 * core Gorm trait for repo methods that use the repo for persistance
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait PersistableRepoEntity<D, R extends GormRepo<D>, ID> extends GormEntity<D> implements Persistable<ID> {

    static R getRepo() { return (R) RepoLookup.findRepo(this) }

    R findRepo() { return (R) RepoLookup.findRepo(getClass()) }

    D persist() {
        return findRepo().persist((D) this, [:])
    }

    D persist(Map args) {
        return findRepo().persist((D) this, args)
    }

    D persist(PersistArgs args) {
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
    static D create(Map data) {
        return getRepo().create(data)
    }

    // static D update(Map args, Map data) {
    //     getRepo().update(data, args)
    // }

    static D update(Map data) {
        getRepo().update(data)
    }

    // static void removeById(Map args = [:], Serializable id) {
    //     getRepo().removeById(id, PersistArgs.of(args))
    // }

    /**
     * default constraints static that calls findConstraints(delegate)
     */
    @CompileDynamic
    static Closure getConstraints(){
        //groovy 3.0.11 hack, the `this` is not working in traits when inside the closure
        Class clazz = this
        return {
            apiConstraints(clazz, getDelegate())
        }
    }

    static void apiConstraints(Class cls, Object builder){
        ApiConstraints.processConstraints(cls, builder)
    }

    static void apiConstraints(Object builder){
        apiConstraints(this, builder)
    }

    @Transient
    boolean isNewOrDirty() {
        findRepo().isNewOrDirty((GormEntity) this)
    }

    //--------------Mango query helpers, mostly for testing-------------

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional optional criteria
     * call get or list on returned object to fire it
     * @param params the mango criteria language map
     * @param closure optional closure
     */
    static MangoDetachedCriteria<D> query(Map params, @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        ((ApiCrudRepo)getRepo()).query(params, closure)
    }

    /**
     * Builds detached criteria for domain, call get or list on it.
     *
     * @return a DetachedCriteria instance
     */
    static MangoDetachedCriteria<D> query(@DelegatesTo(MangoDetachedCriteria) Closure closure) {
        ((ApiCrudRepo)getRepo()).query([:], closure)
    }

    /**
     * Builds detached criteria for domain, call get or list on it.
     *
     * @return a DetachedCriteria instance
     */
    static MangoDetachedCriteria<D> query(QueryArgs queryArgs) {
        ((ApiCrudRepo)getRepo()).query(queryArgs)
    }
}
