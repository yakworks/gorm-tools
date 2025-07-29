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
trait PersistableRepoEntity<D, ID> extends GormEntity<D> implements Persistable<ID> {

    static GormRepo<D> getRepo() { return (GormRepo<D>) RepoLookup.findRepo(this) }

    GormRepo<D> findRepo() { return (GormRepo<D>) RepoLookup.findRepo(getClass()) }

    D persist() {
        return findRepo().persist((D) this, [:])
    }

    D persist(Map args) {
        return findRepo().persist((D) this, args)
    }

    D persist(PersistArgs args) {
        return findRepo().persist((D) this, args)
    }

    void remove() {
        findRepo().remove((D) this)
    }

    void remove(PersistArgs args) {
        findRepo().remove((D) this, args)
    }

    void bind(Map data) {
        findRepo().getEntityMapBinder().bind([:], (D) this, data)
    }

    @Transient
    boolean isNewOrDirty() {
        findRepo().isNewOrDirty((GormEntity) this)
    }

    //--------------static helpers ------------

    static D create(Map data) {
        return getRepo().create(data)
    }

    static D update(Map data) {
        getRepo().update(data)
    }

    /**
     * REPLACE the one in GormEntity as it has a bug and is creating a very bad cross join on same table
     * Checks whether an entity exists
     * @Override GormEntity.exists
     */
    static boolean exists(Serializable id) {
        getRepo().exists(id)
    }

    /**
     * Retrieves and object from the datastore. eg. Book.get(1)
     */
    static D getNotNull(Serializable id) {
        getRepo().getNotNull(id)
    }
    /**
     * default constraints static that calls apiConstraints(delegate)
     */
    @CompileDynamic
    static Closure getConstraints() {
        //groovy 3.0.11 hack, the `this` is not working in traits when inside the closure
        Class clazz = this
        return {
            ApiConstraints.processConstraints(clazz, getDelegate())
        }
    }

    //use this one at beginning of normal 'static constraints = {' block
    static void apiConstraints(Object builder){
        ApiConstraints.processConstraints(this, builder)
    }

    //--------------static Mango query helpers ------------

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
