/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.grails.orm.hibernate.datasource.MultipleDataSourceSupport
import org.hibernate.SessionFactory

import gorm.tools.beans.AppCtx
import gorm.tools.hibernate.criteria.GormHibernateCriteriaBuilder
import gorm.tools.mango.api.QueryMangoEntity
import grails.util.Holders

/**
 * Main trait for a domain. gets applied to them during startup grails artifact part
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait GormRepoEntity<D extends GormEntity<D>> implements QueryMangoEntity<D> {

    Class getEntityClass(){ getClass() }

    private static GormRepo cachedRepo

    abstract private static GormStaticApi<D> currentGormStaticApi()

    /**
     * finds the repo bean in the appctx if cachedRepo is null. returns the cachedRepo if its already set
     * @return The repository
     */
    static GormRepo<D> findRepo() {
        if(!cachedRepo) cachedRepo = AppCtx.get(RepoUtil.getRepoBeanName(this), GormRepo)
        return cachedRepo
    }

    /**
     * Calls the findRepo(). can be overriden to return the concrete domain Repository
     * @return The repository
     */
    transient static GormRepo<D> getRepo() {
        return findRepo()
    }

    transient static void setRepo(GormRepo<D> repo) {
        cachedRepo = repo
    }

    D persist(Map args = [:]) {
        getRepo().persist(args, (D) this)
    }

    // D save(Map args = [:]) {
    //     getRepo().persist(args, (D) this)
    // }
    //
    // D saveApi(Map args = [:]) {
    //     getRepo().saveApi(args, (D) this)
    // }

    void remove(Map args = [:]) {
        getRepo().remove(args, (D) this)
    }

    void bind(Map args = [:], Map data) {
        getRepo().getMapBinder().bind(args, (D) this, data)
    }

    /**
     * Creates, binds and persists and instance
     * @return The created instance
     */
    static D create(Map args = [:], Map data) {
        getRepo().create(args, data)
    }

    static D update(Map args = [:], Map data) {
        getRepo().update(args, data)
    }

    static void removeById(Map args = [:], Serializable id) {
        getRepo().removeById(args, id)
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

    // static withCriteria(@DelegatesTo(Criteria) Closure callable) {
    //     createCriteria().invokeMethod("doCall", callable)
    // }

}
