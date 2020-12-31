/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.api

import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.grails.orm.hibernate.datasource.MultipleDataSourceSupport
import org.hibernate.SessionFactory

import gorm.tools.beans.AppCtx
import gorm.tools.hibernate.criteria.GormHibernateCriteriaBuilder
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import grails.util.Holders

/**
 * core trait for repo methods to add to entity.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait BaseRepoEntity<D> {

    /**
     * finds the repo bean in the appctx if cachedRepo is null. returns the cachedRepo if its already set
     * @return The repository
     */
    static GormRepo<D> findRepo() {
        // if(!cachedRepo) cachedRepo = AppCtx.get(RepoUtil.getRepoBeanName(this), GormRepo)
        // return cachedRepo
        AppCtx.get(RepoUtil.getRepoBeanName(this), GormRepo)
    }

    //getting compile errors when trying to use the static getRepo in RepoGetter
    static GormRepo<D> getRepo() { return findRepo() }

    D persist(Map args = [:]) {
        return getRepo().persist((D) this, args)
    }

    // D save(Map args = [:]) {
    //     getRepo().persist(args, (D) this)
    // }
    //
    // D saveApi(Map args = [:]) {
    //     getRepo().saveApi(args, (D) this)
    // }

    void remove(Map args = [:]) {
        getRepo().remove((D) this, args)
    }

    void bind(Map args = [:], Map data) {
        getRepo().getMapBinder().bind(args, (D) this, data)
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

    // this will fire and event and call beforeValidate on the repo.
    // when cascading to child associations while validated in grail's gorm it doesn't fire a ValidationEvent
    // so we fire our own here. FIXME see line 231 in PersistentEntityValidator where it should fire event and issue PR
    void beforeValidate() {
        getRepo().publishBeforeValidate(this)
    }

    /**
     * Deprecated USE QUERY or WHERE INSTEAD
     *
     * Creates a improved  criteria builder instance
     * make it easier to build criteria with domain bean paths
     * allows
     * eq('invoice.customer.name', 'foo')
     *
     * instead of
     * invoice {
     *      customer {
     *          eq(name)
     *      }
     *  }
     * simliar with eq, like and in
     *
     */
    @Deprecated
    @Override
    @CompileDynamic
    static BuildableCriteria createCriteria() {
        BuildableCriteria builder
        //TODO: temp hack, to prevent unit tests failing
        String datasourceName = MultipleDataSourceSupport.getDefaultDataSource(currentGormStaticApi().persistentEntity)
        boolean isDefault = (datasourceName == ConnectionSource.DEFAULT)
        String suffix = isDefault ? '' : '_' + datasourceName
        try {
            builder = new GormHibernateCriteriaBuilder(this, Holders.applicationContext.getBean("sessionFactory$suffix".toString(), SessionFactory))
            builder.conversionService = currentGormStaticApi().datastore.mappingContext.conversionService
        } catch(IllegalStateException){
            builder = currentGormStaticApi().createCriteria()
        }

        return builder
    }
}
